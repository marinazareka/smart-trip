#include "requesthandler.h"

#include <QSet>
#include <QVariant>
#include <QDebug>

#include <smartslog/generic.h>
#include "ontology/ontology.h"

#include "common.h"
#include "gets.h"


class RequestHandlerData {
public:
    individual_t* individual;
    QSet<QString> pendingCaptGeneratorIds;
    Gets* gets;

    QList<Placemark> points;
};

RequestHandler::RequestHandler(QString userRequestUuid,
                               QMultiMap<QString, QString> captGenerators,
                               QObject* parent)
    : QObject(parent), d(new RequestHandlerData) {
    d->individual = sslog_new_individual(CLASS_USERREQUEST);
    d->gets = new Gets(this);

    connect(d->gets, SIGNAL(pointsLoaded(QVariant,QList<Placemark>)),
            this, SLOT(onPointsLoaded(QVariant,QList<Placemark>)));

    sslog_set_individual_uuid(d->individual, userRequestUuid.toStdString().c_str());
    sslog_ss_populate_individual(d->individual);

    QString objectType = Common::getProperty(d->individual, PROPERTY_OBJECTTYPE).toString();
    d->pendingCaptGeneratorIds = captGenerators.values(objectType).toSet();

    if (d->pendingCaptGeneratorIds.isEmpty()) {
        return;
    }

    for (QString str : d->pendingCaptGeneratorIds) {
        throw std::runtime_error("Not implemented yet");
    }
}

RequestHandler::~RequestHandler() {
    sslog_free_individual(d->individual);
    delete d;
}

bool RequestHandler::isReadyToExecute() const {
    return d->pendingCaptGeneratorIds.isEmpty();
}

void RequestHandler::execute() {
    individual_t* dynamicContext = Common::getIndividualProperty(d->individual,
                                                                 PROPERTY_CONTAINSDYNAMICCONTEXT);
    sslog_ss_populate_individual(dynamicContext);
    double lat = Common::getProperty(dynamicContext, PROPERTY_LAT, false).toDouble();
    double lon = Common::getProperty(dynamicContext, PROPERTY_LON, false).toDouble();


    individual_t* parameters = Common::getIndividualProperty(d->individual,
                                                             PROPERTY_HASSIMPLEREQUESTPARAMETERS);
    sslog_ss_populate_individual(parameters);
    bool radiusOk;
    QString pattern = Common::getProperty(parameters, PROPERTY_PATTERN, false).toString();
    double radius = Common::getProperty(parameters, PROPERTY_RADIUS, false).toDouble(&radiusOk);

    if (!radiusOk) {
        radius = DEFAULT_RADIUS;
    }

    d->gets->requestPoints(QVariant(), lat, lon, pattern, radius);

    // TODO: is dynamic context must be cleared?
}

void RequestHandler::onPointsLoaded(QVariant tag, QList<Placemark> points) {
    d->points = points;

    sslog_ss_add_property(d->individual, PROPERTY_PROCESSED, const_cast<char*>("true"));
}

void RequestHandler::processPageRequest(individual_t* pageRequest) {
    bool convertOk;
    int pageNumber = Common::getProperty(pageRequest, PROPERTY_PAGE, false).toInt(&convertOk);

    if (!convertOk) {
        qDebug() << "Received page request with wrong page number";
        return;
    }

    int fromIdx = PAGE_SIZE * pageNumber;
    int toIdx = qMin(PAGE_SIZE * (pageNumber + 1), d->points.size());

    individual_t* page = sslog_new_individual(CLASS_PAGE);
    Common::setGeneratedId(page, "page");

    qDebug() << "Sending page " << pageNumber << "[" << fromIdx << ":" << toIdx << "]";
    QList<individual_t*> placemarkIndividuals;
    for (int i = fromIdx; i < toIdx ; i++) {
        Placemark placemark = d->points.at(i);

        individual_t* placemarkIndividual = sslog_new_individual(CLASS_PLACEMARK);
        Common::setGeneratedId(placemarkIndividual);
        Common::setProperty(placemarkIndividual, PROPERTY_LAT, placemark.getLat());
        Common::setProperty(placemarkIndividual, PROPERTY_LON, placemark.getLon());

        qDebug() << "Sending placemark "
                 << placemark.getLat() << placemark.getLon() << placemarkIndividual->uuid;


        int code = sslog_ss_insert_individual(placemarkIndividual);
        if (code != 0) {
            qDebug() << "sslog_ss_insert_individual" << code << get_error_text();
        }


        code = sslog_add_property(page, PROPERTY_CONSISTSIN, placemarkIndividual);
        if (code != 0) {
            qDebug() << "sslog_ss_insert_individual" << code << get_error_text();
        }

        placemarkIndividuals.append(placemarkIndividual);
        // TODO: placemark individual must be freed
    }

    sslog_ss_insert_individual(page);

    sslog_ss_add_property(pageRequest, PROPERTY_RESULTSIN, page);
    sslog_ss_add_property(pageRequest, PROPERTY_PROCESSED, const_cast<char*>("true"));


    // Cleanup
    for (individual_t* placemarkIndividual : placemarkIndividuals) {
        sslog_free_individual(placemarkIndividual);
    }

    sslog_free_individual(page);
}
