package org.grobid.core.data.normalization;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.utilities.UnitUtilities;
import org.junit.Before;
import org.junit.Test;
import tec.units.ri.unit.ProductUnit;
import tec.units.ri.unit.TransformedUnit;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by lfoppiano on 14.02.16.
 */
public class NormalizationWrapperTest {

    private NormalizationWrapper target;

    @Before
    public void setUp() throws Exception {
        target = new NormalizationWrapper();
    }

    @Test
    public void testParse_baseUnit_noPow() throws Exception {
        String unitSymbol = "m";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(normalized.getSymbol(), is("m"));
    }

    @Test
    public void testParse_composedUnit_noPow() throws Exception {
        String unitSymbol = "hm";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(((TransformedUnit)normalized).getParentUnit().getSymbol(), is("m"));
    }

    @Test
    public void testParse_baseUnit_pow2() throws Exception {
        String unitSymbol = "m^2";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);
        assertThat(normalized.getProductUnits().size(), is(1));
        String decomposedUnit = "m";

        assertThat(normalized.getSymbol(), is(decomposedUnit));

    }

    @Test
    public void testParse_transformedUnit_pow2() throws Exception {
        String unitSymbol = "km^2";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);
        assertThat(normalized.getProductUnits().size(), is(1));
        String decomposedUnit = "km";
        assertThat(normalized, is(ProductUnit.class));
        assertThat(normalized.getProductUnits().size(), is(1));
    }

    @Test
    public void testParse_productUnit_pow2() throws Exception {
        String unitSymbol = "m/km^2";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);
        Map productUnits = normalized.getProductUnits();
        assertThat(normalized, is(ProductUnit.class));
        assertThat(productUnits.size(), is(2));
    }

    @Test
    public void testParse_productUnit_celsius() throws Exception {
        String unitSymbol = "m*°C";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(normalized.getProductUnits().size(), is(2));

    }


    @Test(expected = NormalizationException.class)
    public void testParse_productUnit_unknown() throws Exception {
        String unitSymbol = "m*J*y";
        target.parseUnit(unitSymbol);
    }

    @Test(expected = NormalizationException.class)
    public void testNormalization_productUnit_unknown2() throws Exception {
        String unitSymbol = "μ*m";
        target.parseUnit(unitSymbol);
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithoutNormalization_meters() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2 m");
        input.setRawValue("2");
        Unit raw = new Unit();
        raw.setRawName("m");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("m"));
        assertThat(output.getNormalizedValue(), is(2.0));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithNormalization_kmToMeters() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2 km");
        input.setRawValue("2");
        Unit raw = new Unit();
        raw.setRawName("km");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("m"));
        assertThat(output.getNormalizedValue(), is(2000.0));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithNormalization_CelsiusToKelvin() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("10°C");
        input.setRawValue("10");
        Unit raw = new Unit();
        raw.setRawName("°C");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getNormalizedUnit().getRawName(), is("K"));
        assertThat(output.getNormalizedValue(), is(283.15));
    }

    @Test
    public void testNormalizeQuantity_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2 km/h");
        input.setRawValue("2");
        Unit raw = new Unit();
        raw.setRawName("km/h");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getNormalizedValue(), is(0.5555555555555556));
    //    assertThat(output.getNormalizedUnit().getRawName(), is("m/s"));
    }

    @Test
    public void testNormalizeQuantity2_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2000 km*g/h");
        input.setRawValue("2000");
        Unit raw = new Unit();
        raw.setRawName("km*g/h");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getNormalizedValue(), is(0.5555555555555556));
    //    assertThat(output.getNormalizedUnit().getRawName(), is("m/s"));
    }

    @Test
    public void testNormalizeQuantity3_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setType(UnitUtilities.Unit_Type.LENGTH);
        input.setRawString("2000 km*kg/h");
        input.setRawValue("2000");
        Unit raw = new Unit();
        raw.setRawName("km*kg/h");
        input.setRawUnit(raw);

        Quantity output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getNormalizedValue(), is(555.5555555555555));
        //    assertThat(output.getNormalizedUnit().getRawName(), is("m/s"));
    }

}