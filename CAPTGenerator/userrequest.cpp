#include "userrequest.h"

#include <QDebug>

#include <smartslog/generic.h>

#include "ontology/ontology.h"

class UserRequestData : public QSharedData {
public:
    UserRequestData();
    ~UserRequestData();

    individual_t* userRequest;
    individual_t* dynamicContext;
    individual_t* user;
    individual_t* staticUserContext;

    prop_val_t* dynamicContextValue;
    prop_val_t* relatesToValue;
    prop_val_t* staticUserContextValue;

    bool isInitialized;
    bool isFreed;
};

UserRequest::UserRequest() {
    data->userRequest = sslog_new_individual(CLASS_USERREQUEST);
}

UserRequest::UserRequest(QString userRequestUuid) : data(new UserRequestData) {
    data->userRequest = sslog_new_individual(CLASS_USERREQUEST);
    sslog_set_individual_uuid(data->userRequest, userRequestUuid.toStdString().c_str());

    data->dynamicContextValue = sslog_ss_get_property(data->userRequest, PROPERTY_CONTAINSDYNAMICCONTEXT);

    if (data->dynamicContextValue == nullptr) {
        qDebug() << "No dynamic context found: " << get_error_text();

        throw std::runtime_error("User request doesn't contains dynamic context");
    }

    data->dynamicContext = reinterpret_cast<individual_t*>(data->dynamicContextValue->prop_value);

    data->relatesToValue = sslog_ss_get_property(data->userRequest, PROPERTY_RELATESTO);
    data->user = reinterpret_cast<individual_t*>(data->relatesToValue->prop_value);

    data->staticUserContextValue = sslog_ss_get_property(data->user, PROPERTY_HASSTATICUSERCONTEXT);
    data->staticUserContext = reinterpret_cast<individual_t*>(data->staticUserContextValue->prop_value);

    sslog_ss_populate_individual(data->staticUserContext);
    sslog_ss_populate_individual(data->dynamicContext);

    data->isInitialized = true;
}

UserRequest::UserRequest(const UserRequest &rhs) : data(rhs.data) {
}

UserRequest &UserRequest::operator=(const UserRequest &rhs) {
    if (this != &rhs)
        data.operator=(rhs.data);
    return *this;
}

UserRequest::~UserRequest() {
}

QVariant UserRequest::getStaticContextProperty(const char* key) {
    const prop_val_t* value = sslog_get_property(data->staticUserContext, key);
    if (value == nullptr) {
        return QVariant();
    }

    const char* strValue = reinterpret_cast<const char*>(value->prop_value);
    return strValue;
}

QVariant UserRequest::getDynamicContextProperty(const char *key) {
    const prop_val_t* value = sslog_get_property(data->dynamicContext, key);
    if (value == nullptr) {
        return QVariant();
    }

    const char* strValue = reinterpret_cast<const char*>(value->prop_value);
    return strValue;
}

individual_t *UserRequest::getUserRequestIndividual() const {
    if (!data->isInitialized) {
        throw std::runtime_error("Invalid state exception");
    }

    return data->userRequest;
}


UserRequestData::UserRequestData() {
    isInitialized = false;
    isFreed = false;
}

UserRequestData::~UserRequestData() {
    if (!isInitialized) {
        return;
    }

    if (isFreed) {
        throw std::runtime_error("Trying to free UserRequestData second time");
    }

    sslog_free_data_property_value_struct(dynamicContextValue);
    sslog_free_data_property_value_struct(staticUserContextValue);
    sslog_free_data_property_value_struct(relatesToValue);

    sslog_free_individual(dynamicContext);
    sslog_free_individual(user);
    sslog_free_individual(staticUserContext);
    sslog_free_individual(userRequest);

    isFreed = true;
}
