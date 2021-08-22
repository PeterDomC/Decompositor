import java.util.*;
import gurobi.*;

public class GRBVarSetALT{

    ArrayList<ArrayList<GRBVar>> XVars;
    HashMap<Integer,HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>>>> YVars;
    HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>> ZVars;
    HashMap<Integer,HashMap<Integer,GRBVar>> AVars;

    public GRBVarSetALT(int s, int k){
        
        XVars = new ArrayList<ArrayList<GRBVar>>(s);
        for (int i = 0; i < s; i++) {
            ArrayList<GRBVar> col = new ArrayList<GRBVar>(k);
            XVars.add(col);
        }

        YVars = new HashMap<Integer,HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>>>>();

        ZVars = new HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>>(s);
        for (int i = 0; i < s; i++) {
            HashMap<Integer,ArrayList<GRBVar>> sec = new HashMap<Integer,ArrayList<GRBVar>>(s-i-1);

            for (int ipr = 0; ipr < s; ipr++) {
                if (ipr > i) {
                    ArrayList<GRBVar> col = new ArrayList<GRBVar>(k);
                    sec.put(ipr,col);
                }
            }

            ZVars.put(i,sec);
        }

        AVars = new HashMap<Integer,HashMap<Integer,GRBVar>>(s);
        for (int i = 0; i < s; i++) {
            HashMap<Integer,GRBVar> sec = new HashMap<Integer,GRBVar>(s-i-1);
            AVars.put(i,sec);
        }
    }

    public void addX(int i, int j, GRBVar x){
        XVars.get(i).add(j,x);
    }

    public void addY(int i_min, int i_max, int l_min, int l_max, ArrayList<GRBVar> y_vars){

        if (YVars.get(i_min) == null) {
            HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>>> sec = new HashMap<Integer,HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>>>();
            YVars.put(i_min,sec);
        }

        if (YVars.get(i_min).get(i_max) == null) {
            HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>> tar = new HashMap<Integer,HashMap<Integer,ArrayList<GRBVar>>>();
            YVars.get(i_min).put(i_max,tar);
        }

        if (YVars.get(i_min).get(i_max).get(l_min) == null) {
            HashMap<Integer,ArrayList<GRBVar>> tar_max = new HashMap<Integer,ArrayList<GRBVar>>();
            YVars.get(i_min).get(i_max).put(l_min,tar_max);
        }

        if (YVars.get(i_min).get(i_max).get(l_min).get(l_max) == null) {
            YVars.get(i_min).get(i_max).get(l_min).put(l_max,y_vars);
        }
    }

    public void addZ(int i, int ipr, int j, GRBVar z){
        ZVars.get(i).get(ipr).add(j,z);
    }

    public void addA(int i, int ipr, GRBVar a){
        AVars.get(i).put(ipr,a);
    }

    public GRBVar getX(int i, int j){
        return XVars.get(i).get(j);
    }

    public GRBVar getY(int i_min, int i_max, int l_min, int l_max, int j){
        return YVars.get(i_min).get(i_max).get(l_min).get(l_max).get(j);
    }

    public GRBVar getZ(int i, int ipr, int j){
        return ZVars.get(i).get(ipr).get(j);
    }

    public GRBVar getA(int i, int ipr){
        return AVars.get(i).get(ipr);
    }

    public boolean containsY(int i_min, int i_max, int l_min, int l_max){
        if (YVars.get(i_min) != null) {
            if (YVars.get(i_min).get(i_max) != null) {
                if (YVars.get(i_min).get(i_max).get(l_min) != null) {
                    return (YVars.get(i_min).get(i_max).get(l_min).get(l_max) != null);
                }
            }
        }

        return false;
    }
}