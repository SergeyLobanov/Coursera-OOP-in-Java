package module1;

/**
 * Created by Сергей on 22.01.2016.
 */
public class ArrayLocation {
    public double coords[];

    public ArrayLocation(double[] coords){
        this.coords = coords;
    }

    public static void main(String[] args) {
        double coords[] = {5.0, 0.0};
        ArrayLocation al = new ArrayLocation(coords);
        coords[0] = 32.0;
        coords[1] = -117.2;
        System.out.println(al.coords[0]);
    }
}
