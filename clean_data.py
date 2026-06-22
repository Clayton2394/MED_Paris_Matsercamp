import os


# 1. Nettoyage du fichier routes.txt
with open("Data/routes.txt", "r", encoding="utf-8") as routes_file:
    routes_lines = routes_file.readlines()

routes_keys = []
routes_agency_id = []
routes_names = []
routes_type = []
routes_color = []

for i in range(1, len(routes_lines)):
    routes_lines[i] = routes_lines[i].split(",")
    if routes_lines[i][5] == "1" and routes_lines[i][1] == "IDFM:Operator_100":
        routes_keys.append(routes_lines[i][0])
        routes_agency_id.append(routes_lines[i][1])
        routes_names.append(routes_lines[i][2])
        routes_type.append(routes_lines[i][5])
        routes_color.append(routes_lines[i][7])

with open("Data_clean/routes_clean.txt", "w", encoding="utf-8") as routes_file_clean:
    for i in range(len(routes_keys)):
        routes_file_clean.write(routes_keys[i] + "," 
                                + routes_agency_id[i] + "," 
                                + routes_names[i] + "," 
                                + routes_type[i] + "," 
                                + routes_color[i] + "\n")


# 2. Nettoyage du fichier trips.txt
with open("Data/trips.txt", "r", encoding="utf-8") as trips_file:
    trips_lines = trips_file.readlines()

trips_route_id = []
trips_service_id = []
trips_keys = []
trips_direction = []

for i in range(1, len(trips_lines)):
    trips_lines[i] = trips_lines[i].split(",")
    if trips_lines[i][0] in routes_keys:
        trips_route_id.append(trips_lines[i][0])
        trips_service_id.append(trips_lines[i][1])
        trips_keys.append(trips_lines[i][2])
        trips_direction.append(trips_lines[i][5])

with open("Data_clean/trips_clean.txt", "w", encoding="utf-8") as trips_file_clean:
    for i in range(len(trips_keys)):
        trips_file_clean.write(trips_route_id[i] + "," + trips_service_id[i] + "," + trips_keys[i] + "," + trips_direction[i] + "\n")


# 3. EXTRACTION DES TEMPS
def time_to_seconds(time_str):
    h, m, s = map(int, time_str.strip().split(':'))
    return h * 3600 + m * 60 + s

trip_to_route = {}
with open("Data_clean/trips_clean.txt", "r", encoding="utf-8") as f_trips:
    for line in f_trips:
        cols = line.strip().split(",")
        trip_to_route[cols[2]] = cols[0] 

seen_edges = set() 
stops_to_keep = set()

previous_trip = None
previous_stop = None
previous_time = 0

with open("Data/stop_times.txt", "r", encoding="utf-8") as stop_times_file, \
     open("Data_clean/liaisons_clean.txt", "w", encoding="utf-8") as liaisons_file:
    
    next(stop_times_file) 
    liaisons_file.write("stop_depart,stop_arrivee,duree_secondes,route_id\n")
    
    for line in stop_times_file:
        columns = line.split(",")
        trip_id = columns[0].strip()
        
        if trip_id in trip_to_route:
            arrival_time = columns[1].strip()
            stop_id = columns[3].strip()
            current_time = time_to_seconds(arrival_time)
            
            if trip_id == previous_trip:
                route_id = trip_to_route[trip_id]
                edge_signature = (previous_stop, stop_id, route_id)
                
                if edge_signature not in seen_edges:
                    duree = current_time - previous_time
                    if duree >= 0:
                        liaisons_file.write(f"{previous_stop},{stop_id},{duree},{route_id}\n")
                        seen_edges.add(edge_signature)
            
            previous_trip = trip_id
            previous_stop = stop_id
            previous_time = current_time
            stops_to_keep.add(stop_id)


# 4. Nettoyage du fichier stops.txt
with open("Data/stops.txt", "r", encoding="utf-8") as stops_file:
    stops_lines = stops_file.readlines()

stops_keys = []
stops_names = []
stops_lon = []
stops_lat = []

for i in range(1, len(stops_lines)):
    columns = stops_lines[i].split(",")
    current_stop_id = columns[0].strip()
    
    if current_stop_id in stops_to_keep:
        stops_keys.append(current_stop_id)
        stops_names.append(columns[2])
        stops_lon.append(columns[4])
        stops_lat.append(columns[5].strip())

with open("Data_clean/stops_clean.txt", "w", encoding="utf-8") as stops_file_clean:
    stops_file_clean.write("stop_id,stop_name,stop_lon,stop_lat\n")
    for i in range(len(stops_keys)):
        stops_file_clean.write(stops_keys[i] + "," + stops_names[i] + "," + stops_lon[i] + "," + stops_lat[i] + "\n")
