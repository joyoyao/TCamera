#pragma version(1)
#pragma rs java_package_name(com.abcew.camera)
#pragma rs_fp_relaxed

uchar alpha;
rs_allocation gIn;// 输入图像的 allocation 对象
rs_allocation gOut;// 输出图像的 allocation 对象
uchar4 __attribute__((kernel)) root(uchar4 in,uint32_t x, uint32_t y) {


     //   uint32_t outCount=y*512+x;
     //   uint32_t cn=outCount/64;
    //    uint32_t cRemainder=outCount%64;


    //   uint32_t timex=x/64;
     //  uint32_t remainderx=x%64;uint32_t
      // uint32_t timey=y/8;
     //  uint32_t remaindery=y%8;
      // uint32_t count=  remainderx*64+timex + remaindery*8+timey*512;



//        Bitmap cache = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);
//
//        Allocation mAllocIn  = Allocation.createFromBitmap(rs, lutBitmap);
//        Allocation mAllocOut = Allocation.createFromBitmap(rs, cache);

      // rsDebug("rsdebug",x);
     uint32_t xyIndex =x+y*512;


      uint32_t b = xyIndex%64;

      uint32_t r = xyIndex /( 64 * 64);
      uint32_t g =   ( xyIndex%(64*64)) / (64);


      uint32_t blockY = b  / 8;
      uint32_t blockX = b % 8;

       uint32_t lut_count = (blockY * 64 + g) * 512 + (blockX * 64 + r);


     // uint32_t lut_y = blockY * 64 + g;
    // uint32_t  lut_x = blockX * 64 + r;

       return rsGetElementAt_uchar4(gIn, lut_count%512 , lut_count/512);
}
