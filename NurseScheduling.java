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

Literal[][][] shifts = new Literal[numNurses][numDays][numShifts];
for (int n : allNurses) {
  for (int d : allDays) {
    for (int s : allShifts) {
      shifts[n][d][s] = model.newBoolVar("shifts_n" + n + "d" + d + "s" + s);
    }
  }
}
#
  }
}