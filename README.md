# Heuristic-stack-of-boxes
Name: Ashutosh Jambhale

The NPCStack program implements a solution for the 3D box stacking problem using a simulated annealing approach.
The goal is to stack boxes to achieve maximum possible height while ensuring each box in the stack has strictly smaller width and length than the box below it, and each box is used only once.

References:
1) Box Stacking Problem (GeeksforGeeks): https://www.geeksforgeeks.org/box-stacking-problem-dp-22/
-Demonstrated the approach of generating all orientations and sorting by base area
2) Heuristics for spatial sorting (StackOverflow): https://stackoverflow.com/questions/37271413/heuristics-to-sort-array-of-2d-3d-points-according-their-mutual-distance


How to Use It:
Compile the code: javac NPCStack.java
Run it: java NPCStack 'input file' 'initial temp' 'cooling rate'

Output Format: width length height remaining_height

Methods:
main: Entry point that handles command-line arguments and coordinates the solution process

makeOrientations: Generates all possible rotations for each box (6 total per box)

greedyStart: Creates initial solution using greedy approach (sorted by base area)

stackHeight: Calculates total height of a stack

cachedHeight: Caches heights to avoid redundant calculations

randomChange: Generates neighboring solutions by making random changes (add/remove/replace boxes)

canBeOn: Checks if one box can be placed on top of another

area: Calculates base area for sorting


How It Works:
1)Reads input file containing box dimensions
2)Generates all possible box orientations (rotations)
3)Creates initial solution using greedy approach (sorted by base area)
4)Applies simulated annealing:
5)Starts with high "temperature" that gradually decreases
6)Makes random changes to current solution
7)Accepts better solutions immediately
8)Sometimes accepts worse solutions (probability decreases with temperature)
9)Outputs the best solution found

Example of how this code works:
Command: java NPCStack cubes0009.boxes 20 0.5
Steps:
1)See Input: 9 cubes (sizes 1×1×1 to 9×9×9)
2)Process:
    -Stacks largest cube (9) at bottom
    -Places next largest (8) on top, and so on down to smallest (1)
    -Result: Perfect tower (height = 45) where each cube fits perfectly inside the one below it
    -Why Best?
	  -Cubes can't be rotated differently (all sides equal)
      -No better arrangement exists
