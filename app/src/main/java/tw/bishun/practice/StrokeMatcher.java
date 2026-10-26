package tw.bishun.practice;

/** Coordinates are normalized to the source's 1024-unit square. */
public final class StrokeMatcher {
    private StrokeMatcher() {}
    public static boolean matches(float[][] input, float[][] target) {
        if (input.length < 2 || target.length < 2) return false;
        if (distance(input[0], target[0]) > 115 || distance(input[input.length-1], target[target.length-1]) > 125) return false;
        float inputLength = length(input), targetLength = length(target);
        if (inputLength < targetLength * .55f || inputLength > targetLength * 1.8f + 40) return false;
        float[][] a = resample(input, 32), b = resample(target, 32);
        float total = 0;
        for (int i=0; i<32; i++) total += distance(a[i], b[i]);
        return total / 32 < 85;
    }
    static float distance(float[] a, float[] b) { return (float)Math.hypot(a[0]-b[0], a[1]-b[1]); }
    static float length(float[][] p) { float n=0; for(int i=1;i<p.length;i++) n+=distance(p[i-1],p[i]); return n; }
    static float[][] resample(float[][] p, int count) {
        float[][] out=new float[count][2]; float step=length(p)/(count-1), walked=0; int segment=1;
        for(int i=0;i<count;i++) {
            float at=step*i;
            while(segment<p.length-1 && walked+distance(p[segment-1],p[segment])<at) { walked+=distance(p[segment-1],p[segment]); segment++; }
            float d=distance(p[segment-1],p[segment]); float t=d==0?0:Math.min(1,(at-walked)/d);
            for(int j=0;j<2;j++) out[i][j]=p[segment-1][j]+t*(p[segment][j]-p[segment-1][j]);
        }
        return out;
    }
}
