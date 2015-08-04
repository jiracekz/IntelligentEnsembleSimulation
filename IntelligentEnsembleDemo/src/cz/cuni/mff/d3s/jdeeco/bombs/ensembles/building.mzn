include "all_disjoint.mzn";

int: component_count;
int: room_count;
set of int: COMPONENTS = 1..component_count;
set of int: ROOMS = 1..room_count;

set of int: ComponentType = 1..3;
int: explorer = 1;
int: defuser = 2;
int: relay = 3;

%array[COMPONENTS] of int: component_id;
array[COMPONENTS] of ComponentType: component_type;
array[COMPONENTS] of int: component_x;
array[COMPONENTS] of int: component_y;
array[COMPONENTS] of var ROOMS: component_room;

array[ROOMS] of int: room_size;
array[ROOMS] of int: room_x;
array[ROOMS] of int: room_y;
array[ROOMS] of var bool: room_exists;
array[ROOMS] of var 0..component_count: room_relay;
array[ROOMS] of var set of COMPONENTS: room_explorers;
array[ROOMS] of var set of COMPONENTS: room_members;
array[ROOMS] of var float: room_fitness;
var float: fitness;

%array[COMPONENTS,ROOMS] of var float: distances = [|20, 80, 50, 111.8 | 50, 78.1, 20, 58.3 | 58.3, 58.3, 50, 50 | 78.1, 50, 80, 58.3 | 102, 128.1, 50, 50|];
array[COMPONENTS,ROOMS] of int: distances;
%array[COMPONENTS,ROOMS] of var int: distances;
%constraint forall(r in ROOMS)(forall(c in COMPONENTS)(distances[c,r]=(abs(component_x[c] - room_x[r]) + abs(component_y[c] - room_y[r]))));

% relays
constraint forall(r in ROOMS)(room_exists[r] = (room_relay[r] != 0));
constraint forall(r in ROOMS)(room_relay[r] = 0 \/ component_type[room_relay[r]] = relay);
constraint forall(r1 in ROOMS)(room_relay[r1] = 0 \/ (forall(r2 in ROOMS)(r1 = r2 \/ room_relay[r1] != room_relay[r2])));

% explorers
constraint forall(r in ROOMS)(room_members[r] = if room_exists[r] then room_explorers[r] union {room_relay[r]} else {} endif);
constraint all_disjoint(room_members);
constraint forall(r in ROOMS)(room_exists[r] -> not (room_relay[r] in room_explorers[r]));
constraint forall(r in ROOMS)(not room_exists[r] -> room_explorers[r] = {});
constraint forall(c in COMPONENTS)(forall(r in ROOMS)(c in room_members[r] -> component_room[c] = r));

% all components must be used
constraint forall(c in COMPONENTS)(exists(r in ROOMS)(c in room_members[r]));

% fitness
constraint forall(r in ROOMS)(room_fitness[r] = if room_exists[r] then -sum(c in COMPONENTS)(if c in room_members[r] then distances[c,r] else 0 endif) else -100 endif);
constraint fitness = sum(r in ROOMS)(room_fitness[r]);

solve maximize fitness;

%output [ "R" ++ show(r) ++ ": exists=" ++ show(room_exists[r]) ++ " relay=C" ++ show(room_relay[r]) ++ " explorers=" ++ show(room_members[r]) ++ " fitness=" ++ show(room_fitness[r]) ++ "\n" | r in ROOMS] ++
%       ["fitness=", show(fitness), "\n"];