# GenMarkOpus
Demonstration Java code to implement Opus atop MongoDB.

## Installation

```
npm install -g mgeneratejs
```

## Example

```
mgeneratejs '{"name": "$name", "age": "$age", "emails": {"$array": {"of": "$email", "number": 3}}}' -n 5
```

Results in:

```
{"name":"Glenn Simmons","age":32,"emails":["ivuge@afovopid.tt","gied@orsin.zw","wuhowbi@con.uk"]}
{"name":"Jane Santiago","age":57,"emails":["oliclon@ohaoni.la","hetoufi@em.ug","ecwawce@sewwato.kn"]}
{"name":"Winifred Martinez","age":59,"emails":["veag@gi.fm","liwfecor@vifbevof.gr","siwluz@habif.gf"]}
{"name":"Helena Chandler","age":65,"emails":["ga@latcon.tr","wur@helmawak.im","ovpifuva@gabruzup.vc"]}
{"name":"Gary Allison","age":30,"emails":["wiko@unuwudu.za","fog@zokje.sh","juppojer@jadi.tl"]}
```

You can also specify a JSON file instead of a JSON string:

```
mgeneratejs template.json -n 5
```
