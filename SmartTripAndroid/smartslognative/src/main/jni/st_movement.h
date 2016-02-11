//
// Created by ivashov on 07.12.15.
//

#ifndef SMARTTRIPANDROID_ST_MOVEMENT_H
#define SMARTTRIPANDROID_ST_MOVEMENT_H

#include "common/st_point.h"

#define MOVEMENT_TIME_BUF_SIZE (100)

struct Movement {
    struct Point point_a;
    struct Point point_b;
    char start_time[MOVEMENT_TIME_BUF_SIZE];
    char end_time[MOVEMENT_TIME_BUF_SIZE];
};

#endif //SMARTTRIPANDROID_ST_MOVEMENT_H
