# TODO
- Calculer les bounds du layergroup à partir d'une emprise
- Adapter l'ip et le port de geoserver: "192.168.xx.xx:8082"
- Adapter le nom du workspace: "MYWORKSPACE"
- Adapter le nom de l'aggregat avec un suffixe: "aggregat_0115c6ec-54d1-43af-a727-8f8c17a9d7bd"



# Récupérer les aggreations de couches existantes

http://192.168.xx.xx:8082/geoserver/rest/workspaces/MYWORKSPACE/layergroups


# Creation d'un layerGroup dans geoserver

Méthode: POST
URL: http://192.168.xx.xx:8082/geoserver/rest/workspaces/MYWORKSPACE/layergroups
Content-Type: application/xml
Authentification: Basic Auth (admin:geoserver)

et avec ce raw body: (avec layer 1: le plus bas (ie: bluemarble))

<layerGroup>
  <name>aggregat_0115c6ec-54d1-43af-a727-8f8c17a9d7bd</name>
  <mode>SINGLE</mode>
  <enabled>true</enabled>
  <advertised>true</advertised> <workspace>
    <name>MYWORKSPACE</name>
  </workspace>
  <publishables>
    <published type="layer">
      <name>MYWORKSPACE:s_5648fbcd8e65963604a6b4305e224278__69ddec48-ecb9-4a75-9ee1-2dd096848033</name>
    </published>
    <published type="layer">
      <name>MYWORKSPACE:s_577604db5c0db6e34e8ee0d8e8d74040__66336714-d8b9-4472-ba06-7adcff84dcc2</name>
    </published>
  </publishables>
  <bounds>
    <minx>200000</minx>
    <maxx>265000</maxx>
    <miny>6235000</miny>
    <maxy>6280000</maxy>
    <crs>EPSG:3857</crs>
  </bounds>
</layerGroup>



# Ajout de projection 3857 dans le cache de tuiles:

Méthode: PUT
URL: http://192.168.xx.xx:8082/geoserver/gwc/rest/layers/MYWORKSPACE:aggregat_0115c6ec-54d1-43af-a727-8f8c17a9d7bd
Content-Type: application/xml
Authentification: Basic Auth (admin:geoserver)

et avec ce raw body:

<GeoServerLayer>
  <name>MYWORKSPACE:aggregat_0115c6ec-54d1-43af-a727-8f8c17a9d7bd</name>
  <enabled>true</enabled>
  <mimeFormats>
    <string>image/png</string>
    <string>image/jpeg</string>
  </mimeFormats>
  <metaWidthHeight>
    <int>1</int> <int>1</int> </metaWidthHeight>
  <gridSubsets>
    <gridSubset>
      <gridSetName>EPSG:4326</gridSetName>
    </gridSubset>
    <gridSubset>
      <gridSetName>EPSG:900913</gridSetName>
    </gridSubset>
    <gridSubset>
      <gridSetName>WebMercatorQuad</gridSetName>
    </gridSubset>
  </gridSubsets>
  <transparent>true</transparent>
  <gutter>0</gutter>
</GeoServerLayer>


# Supprimer l'aggregat à l'issue

Méthode: DELETE
URL: http://192.168.xx.xx:8082/geoserver/rest/workspaces/MYWORKSPACE/layergroups/aggregat_0115c6ec-54d1-43af-a727-8f8c17a9d7bd
Authentification: Basic Auth (admin:geoserver)