package org.grobid.core.data.normalization;

import org.grobid.core.data.Unit;
import org.grobid.core.data.UnitBlock;
import org.grobid.core.data.UnitDefinition;
import org.grobid.core.engines.UnitParser;
import org.grobid.core.lexicon.QuantityLexicon;

import javax.measure.format.ParserException;
import javax.measure.format.UnitFormat;
import javax.measure.spi.ServiceProvider;
import javax.measure.spi.UnitFormatService;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lfoppiano on 23/03/16.
 */
public class UnitNormalizer {

    private final UnitFormat defaultFormatService;

    private UnitParser unitParser;
    private QuantityLexicon quantityLexicon;

    public UnitNormalizer() {
        UnitFormatService formatService = ServiceProvider.current().getUnitFormatService();
        defaultFormatService = formatService.getUnitFormat();

        unitParser = UnitParser.getInstance();
        quantityLexicon = QuantityLexicon.getInstance();
    }


    /**
     * Unit parsing:
     * - infer the name (name + decomposition) from the written
     * form (including inflections), e.g. m <- meters, A/V <- volt per meter
     * - if not found, parse the unit using the Unit CRF model
     */
    public List<UnitBlock> parseToProduct(String rawUnit) {
        String unitName = quantityLexicon.getNameByInflection(rawUnit);

        List<UnitBlock> unitBlockList = new ArrayList<>();
        if (unitName == null) {
            unitBlockList = unitParser.tagUnit(rawUnit);
        } else {
            unitBlockList.add(new UnitBlock(null, unitName, null));
        }

        return unitBlockList;
    }

    public String reformat(List<UnitBlock> unitBlockList) throws NormalizationException {
        javax.measure.Unit parsedUnit = null;
        String unitName = UnitBlock.asString(unitBlockList);
        try {
            parsedUnit = defaultFormatService.parse(unitName);
        } catch (ParserException pe) {
            throw new NormalizationException("The unit " + unitName + " cannot be normalized. It is either not a valid unit " +
                    "or it is not recognized from the available parsers.", new ParserException(new RuntimeException()));
        }

        return parsedUnit.toString();
    }

    public String parseAndReformat(String rawUnit) throws NormalizationException {
        final List<UnitBlock> unitBlockList = parseToProduct(rawUnit);
        return reformat(unitBlockList);

    }

    public Unit parseUnit(Unit rawUnit) throws NormalizationException {
        List<UnitBlock> blocks = parseToProduct(rawUnit.getRawName());

        Unit parsedUnit = new Unit();
        parsedUnit.setOffsetStart(rawUnit.getOffsetStart());
        parsedUnit.setOffsetEnd(rawUnit.getOffsetEnd());
        parsedUnit.setProductBlocks(blocks);
        parsedUnit.setRawName(reformat(blocks));

        return parsedUnit;
    }

    public UnitDefinition findDefinition(Unit unit) {
        return quantityLexicon.lookup(unit);
    }

    public Unit findDefinitionAndUpdate(Unit unit) {
        UnitDefinition definition = findDefinition(unit);

        if (definition != null) {
            unit.setUnitDefinition(definition);
        }
        return unit;
    }


    public void setUnitParser(UnitParser unitParser) {
        this.unitParser = unitParser;
    }

    public void setQuantityLexicon(QuantityLexicon quantityLexicon) {
        this.quantityLexicon = quantityLexicon;
    }
}
