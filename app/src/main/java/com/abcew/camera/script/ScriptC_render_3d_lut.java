package com.abcew.camera.script;

import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.FieldPacker;
import android.support.v8.renderscript.RSRuntimeException;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptC;

/**
 * Created by laputan on 16/12/21.
 */

public class ScriptC_render_3d_lut extends ScriptC {
    private static final String __rs_resource_name = "render_3d_lut";
    private Element __ALLOCATION;
    private Element __I16;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_I16;
    private static final int mExportVarIdx_rsAllocationIn = 0;
    private Allocation mExportVar_rsAllocationIn;
    private static final int mExportVarIdx_rsAllocationLut = 1;
    private Allocation mExportVar_rsAllocationLut;
    private static final int mExportVarIdx_alpha = 2;
    private short mExportVar_alpha;
    private static final int mExportForEachIdx_root = 0;
    private static final int mExportForEachIdx_approximated = 1;

    public ScriptC_render_3d_lut(RenderScript rs) {
        super(rs, "render_3d_lut", Render_3d_lutBitCode.getBitCode32(), Render_3d_lutBitCode.getBitCode64());
        this.__ALLOCATION = Element.ALLOCATION(rs);
        this.__I16 = Element.I16(rs);
        this.__U8_4 = Element.U8_4(rs);
    }

    public synchronized void set_rsAllocationIn(Allocation v) {
        this.setVar(0, v);
        this.mExportVar_rsAllocationIn = v;
    }

    public Allocation get_rsAllocationIn() {
        return this.mExportVar_rsAllocationIn;
    }

    public FieldID getFieldID_rsAllocationIn() {
        return this.createFieldID(0, (Element)null);
    }

    public synchronized void set_rsAllocationLut(Allocation v) {
        this.setVar(1, v);
        this.mExportVar_rsAllocationLut = v;
    }

    public Allocation get_rsAllocationLut() {
        return this.mExportVar_rsAllocationLut;
    }

    public FieldID getFieldID_rsAllocationLut() {
        return this.createFieldID(1, (Element)null);
    }

    public synchronized void set_alpha(short v) {
        if(this.__rs_fp_I16 != null) {
            this.__rs_fp_I16.reset();
        } else {
            this.__rs_fp_I16 = new FieldPacker(2);
        }

        this.__rs_fp_I16.addI16(v);
        this.setVar(2, this.__rs_fp_I16);
        this.mExportVar_alpha = v;
    }

    public short get_alpha() {
        return this.mExportVar_alpha;
    }

    public FieldID getFieldID_alpha() {
        return this.createFieldID(2, (Element)null);
    }

    public KernelID getKernelID_root() {
        return this.createKernelID(0, 58, (Element)null, (Element)null);
    }

    public void forEach_root(Allocation aout) {
        this.forEach_root(aout, (LaunchOptions)null);
    }

    public void forEach_root(Allocation aout, LaunchOptions sc) {
        if(!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            this.forEach(0, (Allocation)null, aout, (FieldPacker)null, sc);
        }
    }

    public KernelID getKernelID_approximated() {
        return this.createKernelID(1, 58, (Element)null, (Element)null);
    }

    public void forEach_approximated(Allocation aout) {
        this.forEach_approximated(aout, (LaunchOptions)null);
    }

    public void forEach_approximated(Allocation aout, LaunchOptions sc) {
        if(!aout.getType().getElement().isCompatible(this.__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        } else {
            this.forEach(1, (Allocation)null, aout, (FieldPacker)null, sc);
        }
    }
}
