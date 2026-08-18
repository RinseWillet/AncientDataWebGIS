<?xml version="1.0" encoding="UTF-8"?>
<!--
  Shared DEM elevation colour ramp (E3-5). One style, applied to every DEM-category
  elevation layer in GeoServer (not the hillshade siblings, which stay on GeoServer's
  default greyscale style), so the same colour means the same elevation across all of
  them (Swalmen, Venlo-Geldern, Mönchengladbach x2, the Gelderland-NRW regional layer)
  rather than each layer stretching its own local min/max independently.

  Range chosen from the actual measured min/max across all five published DEMs
  (gdalinfo -stats, August 2026):
    Swalmen:                    0.000 to 104.680
    Venlo-Geldern:               3.790 to  83.870
    Mönchengladbach-west:      -21.070 to 151.210
    Mönchengladbach-Neuss:       0.000 to  98.530
    Gelderland-NRW (regional):   0.671 to 121.290
  Combined: -21.07 to 151.21 -> ramp padded slightly to -25/155.

  This intentionally does NOT try to reveal archaeological microrelief - that's the
  multidirectional hillshade layer's job (see "DEM-specific conversion" in
  E3.1-raster-publishing-pipeline.md), rendered as a separate, independently-toggleable
  layer blended on top via mix-blend-mode: multiply in the frontend
  (see `.physical-layer--hillshade` in MapContent.css). This ramp exists for the
  regional geomorphology reading (Veluwe/Reichswald moraine highs vs. the Rhine
  valley/Maas lowlands) at small-to-mid zoom.
-->
<StyledLayerDescriptor version="1.0.0"
    xmlns="http://www.opengis.net/sld"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>dem-elevation-ramp</Name>
    <UserStyle>
      <Title>DEM Elevation Colour Ramp</Title>
      <Abstract>Shared low-relief terrain ramp, -25m to 155m, for all published DEM layers.</Abstract>
      <FeatureTypeStyle>
        <Rule>
          <RasterSymbolizer>
            <Opacity>1.0</Opacity>
            <ColorMap type="ramp">
              <ColorMapEntry color="#2c1a4d" quantity="-25"  label="-25m (mining district low)" />
              <ColorMapEntry color="#3a6ea5" quantity="0"    label="0m" />
              <ColorMapEntry color="#7fb069" quantity="25"   label="25m" />
              <ColorMapEntry color="#c9d16b" quantity="50"   label="50m" />
              <ColorMapEntry color="#e8b84b" quantity="75"   label="75m" />
              <ColorMapEntry color="#c9772e" quantity="100"  label="100m" />
              <ColorMapEntry color="#8b4a2b" quantity="125"  label="125m" />
              <ColorMapEntry color="#f2f2f2" quantity="155"  label="155m+ (moraine crests)" />
            </ColorMap>
          </RasterSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
