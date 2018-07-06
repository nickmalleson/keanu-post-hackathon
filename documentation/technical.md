# Technical documentation

_Explain the code etc_

#StationSim Code


##Classes in StationSim

###Station
This is the simulation state. It contains the scheduler, the presentation of space, (replaceable random number generator) The parameters for the simulation are set here. A main class to run a headless simulation is also found here. 

###StationGUI
This is the visualization class for the simulation allows the user to view the simulation in a GUI and view graphs etc. This class contains its own main method. It is not necessary to us this class at all iv the GUI or visualization is not required 

###Agent
A base parent class for the agents in StationSim. All agents have a size (which is used differently be different agent types), location and a name. The simulation state (station) is also defined here. This is updated at each step and is used to give agents access to the simulation methods. 

###Entrance
An agent which creates people at a given number of steps. The number of people to be created by each entrance is calculated using integer division (total number of people / number of entrances)

###Exit
An agent which removes people from the simulation when they pass through. Exit interval controls have often (in steps) that exits can remove people, until that point people must queue. The point for which people aim actually aim for is a point behind the exit, this allows a spread of people on the exit.  

###Person
An agent which is assigned an entrance to be spawned from and an exit to aim for. Each person is also assigned a desired speed at which they will travel unless otherwise restricted. 

###Sequencer
The sequencer ordered the people in the simulation from each step based on how close they are from their assigned exit so that people who are closer will move first.

###Wall
Locations on the exit wall through which people agent cannot pass (The actual restriction in movement is coll tolled by person objects). Using walls prevent agents from moving around the back of exits when crowding occurs.

###Analysis
A class containing methods for aggregating and writing out data from the simulation. An analysis object is scheduled as an agent to be called at each step.

###Data
A data class for containing the location, speed, entrance, exit ect for a person agent. This is used by the analysis class and not for controlling the simulation itself.