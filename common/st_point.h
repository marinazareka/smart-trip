//
// Created by ivashov on 02.12.15.
//

#ifndef SMARTTRIPANDROID_ST_POINT_H
#define SMARTTRIPANDROID_ST_POINT_H


struct Point {
    char* id;
    char* title;
    double lat;
    double lon;
};

void st_init_point_clone(struct Point* point, struct Point* source);
void st_init_point(struct Point* point, const char* id, const char* title, double lat, double lon);
void st_free_point(struct Point* point);
void st_free_point_array(struct Point* point_array, int size);

#endif //SMARTTRIPANDROID_ST_POINT_H

