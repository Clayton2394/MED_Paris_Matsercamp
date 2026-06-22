import os

# Nettoyage du fichier routes.txt
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

if not os.path.exists("Data_clean/routes_clean.txt"):
    with open("Data_clean/routes_clean.txt", "w", encoding="utf-8") as routes_file_clean:
        for i in range(len(routes_keys)):
            routes_file_clean.write(routes_keys[i] + "," 
                                    + routes_agency_id[i] + "," 
                                    + routes_names[i] + "," 
                                    + routes_type[i] + "," 
                                    + routes_color[i] 
                                    + "\n")


# Nettoyage du fichier trips.txt
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

if not os.path.exists("Data_clean/trips_clean.txt"):
    with open("Data_clean/trips_clean.txt", "w", encoding="utf-8") as trips_file_clean:
        for i in range(len(trips_keys)):
            trips_file_clean.write(trips_route_id[i] + "," 
                                   + trips_service_id[i] + "," 
                                   + trips_keys[i] + "," 
                                   + trips_direction[i] 
                                   + "\n")


# Nettoyage du fichier stop_times.txt
trips_keys_set = set(trips_keys)

stops_to_keep = set() 

if not os.path.exists("Data_clean/stop_times_clean.txt"):
    print("Nettoyage de stop_times.txt en cours (cela peut prendre quelques secondes)...")
    
    with open("Data/stop_times.txt", "r", encoding="utf-8") as stop_times_file, \
         open("Data_clean/stop_times_clean.txt", "w", encoding="utf-8") as stop_times_file_clean:
        
        next(stop_times_file) 
        
        for line in stop_times_file:
            columns = line.split(",")
            
            trip_id = columns[0]
            
            if trip_id in trips_keys_set:
                stop_id = columns[3]
                stop_sequence = columns[4]
                
                stop_times_file_clean.write(f"{trip_id},{stop_id},{stop_sequence}\n")
                
                stops_to_keep.add(stop_id)
                
    print("Nettoyage de stop_times.txt terminé !")

# Nettoyage du fichier stops.txt
with open("Data/stops.txt", "r", encoding="utf-8") as stops_file:
    stops_lines = stops_file.readlines()

stops_keys = []
stops_names = []
stops_lon = []
stops_lat = []

for i in range(1, len(stops_lines)):
    stops_lines[i] = stops_lines[i].split(",")
    if stops_lines[i][0] in stops_to_keep:
        stops_keys.append(stops_lines[i][0])
        stops_names.append(stops_lines[i][2])
        stops_lon.append(stops_lines[i][4])
        stops_lat.append(stops_lines[i][5])


if not os.path.exists("Data_clean/stops_clean.txt"):
    with open("Data_clean/stops_clean.txt", "w", encoding="utf-8") as stops_file_clean:
        for i in range(len(stops_keys)):
            stops_file_clean.write(stops_keys[i] + "," 
                                   + stops_names[i] + "," 
                                   + stops_lon[i] + "," 
                                   + stops_lat[i] 
                                   + "\n")

with open("Data_clean/stops_clean.txt", "r", encoding="utf-8") as stops_file:
    stops_lines = stops_file.readlines()

print(len(stops_lines))