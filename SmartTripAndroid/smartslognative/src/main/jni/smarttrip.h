#ifndef SMARTTRIPANDROID_SMARTTRIP_H
#define SMARTTRIPANDROID_SMARTTRIP_H

struct Point;
struct Movement;

struct Callback {
};

void st_initialize(const char* user_id);
void st_shutdown();
void st_update_user_location(double lat, double lon);
void st_post_search_request(double radius, const char *pattern);
void st_post_schedule_request(struct Point* points, int points_count);

void st_on_search_request_ready(struct Point* points, int points_count);
void st_on_schedule_request_ready(struct Movement* movements, int movements_count);

#endif //SMARTTRIPANDROID_SMARTTRIP_H
