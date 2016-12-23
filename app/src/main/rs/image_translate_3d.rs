#pragma version(1)
#pragma rs java_package_name(com.abcew.camera)
#pragma rs_fp_relaxed

uchar alpha;
rs_allocation gIn;// 输入图像的 allocation 对象
rs_allocation gOut;// 输出图像的 allocation 对象
rs_script gScript;// RenderScript 对象

uchar4 __attribute__((kernel)) root(uchar4 in) {
    uchar4 out = in;
    out.r = in.r;
    out.g = in.g;
    out.b = in.b;

    return out;
}
