#!/usr/bin/env python3

import fileinput

coords = []
for line in fileinput.input():
    linesplit = line.split()
    lat = float(linesplit[0])
    lon = float(linesplit[1])
    coords.append( (lat, lon) )

print('<gpx version="1.1" creator="list2gpx.py">')
print('<trk>')
print('<trkseg>')

for coord in coords:
    print('<trkpt lat="{}" lon="{}" />'.format(coord[0], coord[1]))

print('</trkseg>')
print('</trk>')
print('</gpx>')

