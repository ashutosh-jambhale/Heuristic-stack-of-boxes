/*
Author: Ashutosh Jambhale
Initial Solution Heuristic:
1. Sort all box orientations by bottom face area (width × depth) in descending order.
2. For each box as a potential base, greedily build a stack by adding compatible
    boxes that satisfy the touching faces and single-use conditions.
3. Select the tallest valid stack as the initial solution.
Reference:
References:
1. Box Stacking Problem (GeeksforGeeks): https://www.geeksforgeeks.org/box-stacking-problem-dp-22/
   - Demonstrated the approach of generating all orientations and sorting by base area
2. Heuristics for spatial sorting (StackOverflow): https://stackoverflow.com/questions/37271413/heuristics-to-sort-array-of-2d-3d-points-according-their-mutual-distance
*/
import java.io.*;
import java.util.*;

public class NPCStack {
    static class Box {
        int w, l, h, id; // width, length, height, and unique identifier

        Box(int w, int l, int h, int id) {  
            this.w = w;
            this.l = l;
            this.h = h;
            this.id = id;
        }
        
        int area() { // Calculate base area for sorting
            return w * l;
        }

        boolean canBeOn(Box bottom) { // Check if this box can be placed on top of another box
            return this.w < bottom.w && this.l < bottom.l;
        }

        @Override
        public String toString() {
            return w + " " + l + " " + h;
        }

        @Override  // Equality comparison for caching
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Box)) return false;
            Box b = (Box) o;
            return w == b.w && l == b.l && h == b.h && id == b.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(w, l, h, id);
        }
    }

    static List<Box> makeOrientations(List<Box> input) {  // Generate all possible rotations for each box
        List<Box> all = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            Box box = input.get(i);
            int[] dims = {box.w, box.l, box.h}; 
            // Generate all 3 possible orientations
            for (int j = 0; j < 3; j++) {
                int height = dims[j];
                int side1 = dims[(j + 1) % 3];
                int side2 = dims[(j + 2) % 3];
                // Store with sorted base dimensions
                int min = Math.min(side1, side2);
                int max = Math.max(side1, side2);
                all.add(new Box(min, max, height, i));
            }
        }
        return all;
    }

    static List<Box> greedyStart(List<Box> boxes) {   // create initial solution using greedy approach
        boxes.sort((a, b) -> b.area() - a.area());  // Sort by base area desc
        List<Box> stack = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        for (Box box : boxes) {  // Build stack by adding largest possible box that fits
            if (!used.contains(box.id) && (stack.isEmpty() || box.canBeOn(stack.get(stack.size() - 1)))) {
                stack.add(box);
                used.add(box.id);
            }
        }
        return stack;
    }

    static int stackHeight(List<Box> stack) {  // Calculate total height of a stack
        int total = 0;
        for (Box box : stack) total += box.h;
        return total;
    }

    static int cachedHeight(List<Box> stack, Map<List<Box>, Integer> cache) {  // Cache heights to avoid redundant calculations
        return cache.computeIfAbsent(stack, NPCStack::stackHeight);
    }

    // generate a neighboring solution by making random changes
    static List<Box> randomChange(List<Box> allBoxes, List<Box> current) {  
        Random rand = new Random();
        List<Box> neighbor = new ArrayList<>(current);
        Set<Integer> used = new HashSet<>();
        for (Box box : neighbor) used.add(box.id);

        int choice = rand.nextInt(3); // 0 = add, 1 = remove, 2 = replace

        if (choice == 0 && neighbor.size() < allBoxes.size()) {
            for (Box box : allBoxes) { // Add a new box if possible
                if (!used.contains(box.id)) {
                    if (neighbor.isEmpty() || box.canBeOn(neighbor.get(neighbor.size() - 1))) {
                        neighbor.add(box);
                        break;
                    }
                }
            }
        } 
        else if (choice == 1 && !neighbor.isEmpty()) {
            neighbor.remove(rand.nextInt(neighbor.size()));  // Remove a random box
        } 
        else if (choice == 2 && !neighbor.isEmpty()) {
            int i = rand.nextInt(neighbor.size());   // Replace a random box with another valid box
            for (Box box : allBoxes) {
                if (!used.contains(box.id)) {
                    boolean fits =
                        (i == 0 || box.canBeOn(neighbor.get(i - 1))) &&
                        (i == neighbor.size() - 1 || neighbor.get(i + 1).canBeOn(box));
                    if (fits) {
                        neighbor.set(i, box);
                        break;
                    }
                }
            }
        }
        return neighbor;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java NPCStack 'input file' 'initial temp' 'cooling rate'");
            return;
        }

        String inputFile = args[0];
        double temp = Double.parseDouble(args[1]);
        double coolingRate = Double.parseDouble(args[2]);

        List<Box> original = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                if (parts.length != 3) continue;
                try {
                    int a = Integer.parseInt(parts[0]);
                    int b = Integer.parseInt(parts[1]);
                    int c = Integer.parseInt(parts[2]);
                    if (a > 0 && b > 0 && c > 0) {
                        original.add(new Box(a, b, c, original.size()));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        List<Box> allBoxes = makeOrientations(original);
        List<Box> current = greedyStart(allBoxes);

        int initHeight = stackHeight(current);
        int initSize = current.size();

        List<Box> best = new ArrayList<>(current);   // track best solution found
        int bestHeight = initHeight;

        Random rand = new Random();
        Map<List<Box>, Integer> heightCache = new HashMap<>();

        while (temp > 0) {  // Simulated annealing main loop
            List<Box> neighbor = randomChange(allBoxes, current);
            int curHeight = cachedHeight(current, heightCache);
            int newHeight = cachedHeight(neighbor, heightCache);

            if (newHeight > curHeight) {
                current = neighbor;
                if (newHeight > bestHeight) {
                    best = neighbor;
                    bestHeight = newHeight;
                }
            } else {
                double prob = Math.exp((newHeight - curHeight) / temp);  // sometimes accept worse solutions based on temp
                if (prob > rand.nextDouble()) {
                    current = neighbor;
                }
            }

            temp = temp - coolingRate;
        }

        int remain = stackHeight(best);
        Collections.reverse(best);
        for (Box box : best) {
            System.out.println(box.w + " " + box.l + " " + box.h + " " + remain);
            remain -= box.h;
        }

        System.out.println("\n- Summary -");
        System.out.println("Initial stack size         : " + initSize);
        System.out.println("Initial stack height       : " + initHeight);
        System.out.println("Final stack total height   : " + bestHeight);
    }
}
