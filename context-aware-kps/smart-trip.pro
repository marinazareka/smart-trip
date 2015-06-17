TEMPLATE = subdirs
SUBDIRS += CAPTGenerator PQE RestaurantGenerator restaurant-user-kp

RestaurantGenerator.depends = CAPTGenerator
PQE.depends = CAPTGenerator
