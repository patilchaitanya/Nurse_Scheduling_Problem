import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverSolutionCallback;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class NurseScheduling {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();

// Data 
final int numNurses = 5;
final int numDays = 7;
final int numShifts = 3;

final int[] allNurses = IntStream.range(0, numNurses).toArray();
final int[] allDays = IntStream.range(0, numDays).toArray();
final int[] allShifts = IntStream.range(0, numShifts).toArray();

// Set of triples, corresponding to the three shifts per day.
// Each element of the triple is 0 or 1, indicating whether a shift was requested. For example, the triple [0, 0, 1] in the fifth position of row 1 indicates that nurse 1 requests shift 3 on day 5.
final int[][][] shiftRequests = new int[][][] {
    {
        {0, 0, 1},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 1},
        {0, 1, 0},
        {0, 0, 1},
    },
    {
        {0, 0, 0},
        {0, 0, 0},
        {0, 1, 0},
        {0, 1, 0},
        {1, 0, 0},
        {0, 0, 0},
        {0, 0, 1},
    },
    {
        {0, 1, 0},
        {0, 1, 0},
        {0, 0, 0},
        {1, 0, 0},
        {0, 0, 0},
        {0, 1, 0},
        {0, 0, 0},
    },
    {
        {0, 0, 1},
        {0, 0, 0},
        {1, 0, 0},
        {0, 1, 0},
        {0, 0, 0},
        {1, 0, 0},
        {0, 0, 0},
    },
    {
        {0, 0, 0},
        {0, 0, 1},
        {0, 1, 0},
        {0, 0, 0},
        {1, 0, 0},
        {0, 1, 0},
        {0, 0, 0},
    },
};

CpModel model = new CpModel();

// Array : possible combos of shifts to store if nurse works on that day and shift
Literal[][][] shifts = new Literal[numNurses][numDays][numShifts];
for (int n : allNurses) {
  for (int d : allDays) {
    for (int s : allShifts) {
      shifts[n][d][s] = model.newBoolVar("shifts_n" + n + "d" + d + "s" + s);
    }
  }
}

// Constraints
// 1. Each shift is assigned to a single nurse per day. 
for (int d : allDays) {
    for (int s : allShifts) {
      List<Literal> nurses = new ArrayList<>();
      for (int n : allNurses) {
        nurses.add(shifts[n][d][s]);
      }
      model.addExactlyOne(nurses);
    }
  }

// 2. Each nurse works at most one shift per day.

for (int n : allNurses) {
    for (int d : allDays) {
      List<Literal> work = new ArrayList<>();
      for (int s : allShifts) {
        work.add(shifts[n][d][s]);
      }
      model.addAtMostOne(work);
    }
  }

// 3. Assign shifts evenly

/* Since there are nine shifts over the three-day period, we can assign two shifts to each of the four nurses. After that there will be one shift left over, which can be assigned to any nurse. */

// Try to distribute the shifts evenly, so that each nurse works
// minShiftsPerNurse shifts. If this is not possible, because the total
// number of shifts is not divisible by the number of nurses, some nurses will
// be assigned one more shift.
int minShiftsPerNurse = (numShifts * numDays) / numNurses;
int maxShiftsPerNurse;
if ((numShifts * numDays) % numNurses == 0) {
  maxShiftsPerNurse = minShiftsPerNurse;
} else {
  maxShiftsPerNurse = minShiftsPerNurse + 1;
}
for (int n : allNurses) {
  LinearExprBuilder shiftsWorked = LinearExpr.newBuilder();
  for (int d : allDays) {
    for (int s : allShifts) {
      shiftsWorked.add(shifts[n][d][s]);
    }
  }
  model.addLinearConstraint(shiftsWorked, minShiftsPerNurse, maxShiftsPerNurse);
}

// Give all possible solutions
CpSolver solver = new CpSolver();
solver.getParameters().setLinearizationLevel(0);
// Tell the solver to enumerate all solutions.
solver.getParameters().setEnumerateAllSolutions(true);

final int solutionLimit = 5;
class VarArraySolutionPrinterWithLimit extends CpSolverSolutionCallback {
  public VarArraySolutionPrinterWithLimit(
      int[] allNurses, int[] allDays, int[] allShifts, Literal[][][] shifts, int limit) {
    solutionCount = 0;
    this.allNurses = allNurses;
    this.allDays = allDays;
    this.allShifts = allShifts;
    this.shifts = shifts;
    solutionLimit = limit;
  }

  // callback function
  @Override
  public void onSolutionCallback() {
    System.out.printf("Solution #%d:%n", solutionCount);
    for (int d : allDays) {
      System.out.printf("Day %d%n", d);
      for (int n : allNurses) {
        boolean isWorking = false;
        for (int s : allShifts) {
          if (booleanValue(shifts[n][d][s])) {
            isWorking = true;
            System.out.printf("  Nurse %d work shift %d%n", n, s);
          }
        }
        if (!isWorking) {
          System.out.printf("  Nurse %d does not work%n", n);
        }
      }
    }
    solutionCount++;
    if (solutionCount >= solutionLimit) {
      System.out.printf("Stop search after %d solutions%n", solutionLimit);
      stopSearch();
    }
  }

  public int getSolutionCount() {
    return solutionCount;
  }

  private int solutionCount;
  private final int[] allNurses;
  private final int[] allDays;
  private final int[] allShifts;
  private final Literal[][][] shifts;
  private final int solutionLimit;
}

VarArraySolutionPrinterWithLimit cb =
    new VarArraySolutionPrinterWithLimit(allNurses, allDays, allShifts, shifts, solutionLimit);

// Invoke solver
CpSolverStatus status = solver.solve(model, cb);
System.out.println("Status: " + status);
System.out.println(cb.getSolutionCount() + " solutions found.");

  }
}