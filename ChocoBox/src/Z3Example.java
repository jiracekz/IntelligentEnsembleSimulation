import java.util.HashMap;

import com.microsoft.z3.ApplyResult;
import com.microsoft.z3.BitVecSort;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Goal;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Tactic;


public class Z3Example {

	public static void main(String[] args) throws Exception {
		HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);
		
		System.out.println("LogicTest");

        com.microsoft.z3.Global.ToggleWarningMessages(true);

        BitVecSort bvs = ctx.mkBitVecSort(32);
        Expr x = ctx.mkConst("x", bvs);
        Expr y = ctx.mkConst("y", bvs);
        BoolExpr eq = ctx.mkEq(x, y);

        // Use a solver for QF_BV
        Solver s = ctx.mkSolver("QF_BV");
        s.add(eq);
        Status res = s.check();
        System.out.println("solver result: " + res);

        // Or perhaps a tactic for QF_BV
        Goal g = ctx.mkGoal(true, false, false);
        g.add(eq);

        Tactic t = ctx.mkTactic("qfbv");
        ApplyResult ar = t.apply(g);
        System.out.println("tactic result: " + ar);

        if (ar.getNumSubgoals() != 1 || !ar.getSubgoals()[0].isDecidedSat())
            throw new Exception("failed");
	}

}
