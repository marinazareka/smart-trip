#include <stdlib.h>
#include <unistd.h>
#include <signal.h>

#include <smartslog.h>

#include "ontology.h"
#include "common.h"

bool init(const char* name, const char* smartspace, const char* address, int port);
void shutdown();                                                                  
  
bool subscribe();                                                                
void unsubscribe();                                                             
   
bool wait_subscription(int* out_points_count, double** out_points_pairs, void** data);
void publish(int points_count, double* points_pairs, const char* roadType, void* data); 

int main() {
    init("test_kp", "X", "127.0.0.1", 10010);

    subscribe();

    int count;
    double* points;
    void* data;

    int i = 0;
    while (wait_subscription(&count, &points, &data)) {
        fprintf(stderr, "%d: Result received %d points\n", i++, count);
        publish(count, points, "foot", data);
    }


    unsubscribe();

    shutdown();
}

