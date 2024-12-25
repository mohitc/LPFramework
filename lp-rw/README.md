# LP Model Read/Write support

The module lp-rw enables exporting the model to the JSON/YAML format, and can be used to import a generated model in the
specified formal back into a file. The default format used is YAML to make it human-readable. Code examples to
import/export the LPModel to a file can be found in the
test [`LPModelParserTest`](./src/test/kotlin/com/lpapi/model/parser/LPModelParserTest.kt).

The export mechanism is structured to export the model, and the associated computation result in different files, to
simplify the use case of defining a generic model that is run with different conditions. An example of
exporting/importing the model, and the associated result is shown below: 

```kotlin
val parser = LPModelParser(LPModelFormat.YAML) // define a parser to read-write files in the YAML format 
// Write the model and the associated result to a file. Status of the operation is returned as a boolean
if (!parser.writeToFile(model, fileName)) {
  log.warn{"Could not write $model to $fileName"}
}
if (!parser.writeResultToFile(model, resultFileName)) {
  log.warn{"Could not write result for $model to $resultFileName"}
}

// Read model and result from filename
val newModel = lpModelParser.readFromFile(fileName)
if (newModel == null) {
  log.warn{"Could not read the model as defined in $fileName"}
}
lpModelParser.readResultFromFile(resultFileName, newModel!!)
```
