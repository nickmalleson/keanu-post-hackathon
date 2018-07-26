# Technical documentation

_Explain the code etc_

# StationSim Code


## Classes in StationSim

### Station
This is the simulation state. It contains the scheduler, the presentation of space, (replaceable random number generator) The parameters for the simulation are set here. A main class to run a headless simulation is also found here. 

### StationGUI
This is the visualization class for the simulation allows the user to view the simulation in a GUI and view graphs etc. This class contains its own main method. It is not necessary to us this class at all iv the GUI or visualization is not required 

### Agent
A base parent class for the agents in StationSim. All agents have a size (which is used differently be different agent types), location and a name. The simulation state (station) is also defined here. This is updated at each step and is used to give agents access to the simulation methods. 

### Entrance
An agent which creates people at a given number of steps. The number of people to be created by each entrance is calculated using integer division (total number of people / number of entrances)

### Exit
An agent which removes people from the simulation when they pass through. Exit interval controls have often (in steps) that exits can remove people, until that point people must queue. The point for which people aim actually aim for is a point behind the exit, this allows a spread of people on the exit.  

### Person
An agent which is assigned an entrance to be spawned from and an exit to aim for. Each person is also assigned a desired speed at which they will travel unless otherwise restricted. 

### Sequencer
The sequencer ordered the people in the simulation from each step based on how close they are from their assigned exit so that people who are closer will move first.

### Wall
Locations on the exit wall through which people agent cannot pass (The actual restriction in movement is coll tolled by person objects). Using walls prevent agents from moving around the back of exits when crowding occurs.

### Analysis
A class containing methods for aggregating and writing out data from the simulation. An analysis object is scheduled as an agent to be called at each step.

### Data
A data class for containing the location, speed, entrance, exit ect for a person agent. This is used by the analysis class and not for controlling the simulation itself.


## Overview of how station sim works

	entranceInterval = How often an entrance will allow pwople to enter simulation
	exitInterval = How often an exit will allow people to pass through an Exit simlation
	entranceSize = The number of people who may pass through an entrance in a single step
	exitSize = The number of people who may pass through an exit in a single step
	toatoalnumPeople = Total number of people that can enter throughout the run of the simulation
	
	
	equally space entrances along y axis at x = 0
	equally space exits along y axis at x = width of area
	
	
	for each entrance:
		peopleRemainigForEntrance  = totalNumPeople / number of entrances (integer is division used)
	

	for each simulation step: 
		if step % exitInterval == 0:
			for each exit:
				allow numer of people through exit equal to exitSize
	
		sequencer = All people sorted ascendingly by euclidean distance to their target exit
		
		for each person in sequence:
		
			for i in 0 to slowingDistance:
				testPosition = new coorinates calculated by linear interpolation toward exit
				if testPosition collides with another person:
					reduce speedFactor
					break
		
			newPosition = new coordinantes calculated by linear interpolation towards exit
			if newPosition does not collide with another person:
				current postion = new position
			else:
				new position = randomly choose direction on y axis and calculate distance
				if newPostition does not collide with other people:
					currentPosition = newPosition
				else:
					newPostion = chosse other diection on y axis an calculate diststance
					if newPosition does not collide with other people:
						currentPosition = new Position
				
		if step % entrance interval == 0:
			for each entrance:
				create number of people equal to entrance size
				peopleRemainingForEntrance -= number of people created
				for each person:
					choose target exit using probailities fom exitChoices
					choose maxSpeedFactor from uniform ditribution
		
