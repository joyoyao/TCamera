#pragma version(1)
#pragma rs java_package_name(com.abcew.camera)
#pragma rs_fp_relaxed

uchar alpha;
rs_allocation gIn;// 输入图像的 allocation 对象
rs_allocation gOut;// 输出图像的 allocation 对象
uchar4 __attribute__((kernel)) root(uchar4 in,uint32_t x, uint32_t y) {
         uint32_t timex=x/64;
       uint32_t remainderx=x%64;
       uint32_t timey=y/8;
       uint32_t remaindery=y%8;
       uint32_t count=  remainderx*64+timex + remaindery*8+timey*512;
       return rsGetElementAt_uchar4(gIn, count%512, count/512);
}
