package io.swagger.codegen.v3.generators.dotnet;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.io.File;

import static io.swagger.codegen.v3.CodegenConstants.HAS_ENUMS_EXT_NAME;
import static io.swagger.codegen.v3.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.v3.generators.handlebars.ExtensionHelper.getBooleanValue;

public class CSharpRefitCodegen extends AbstractCSharpCodegen {

    // source folder where to write the files

    // TODO should be packageVersion ?
    protected String apiVersion = "1.0.0";
    protected String projectGuidLibrary = "{" + java.util.UUID.randomUUID().toString().toUpperCase() + "}";
    protected String projectGuidShared = "{" + java.util.UUID.randomUUID().toString().toUpperCase() + "}";

    protected String clientPackage = "Client";
    protected Map<Character, String> regexModifiers;


    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "csharprefit";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a CSharpRefitCodegen client library.";
    }

    public CSharpRefitCodegen() {
        super();

        addOption(CodegenConstants.PACKAGE_NAME,
                "C# package name (convention: Title.Case).",
                this.packageName);

        /**
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put(
                "model.mustache", // the template to use
                ".cs");       // the extension for each file to write

        /**
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
                "api.mustache",   // the template to use
                ".cs");       // the extension for each file to write

        /**
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        //templateDir = getName();

        /**
         * Api Package.  Optional, if needed, this can be used in templates
         */
        apiPackage = "Api";

        /**
         * Model Package.  Optional, if needed, this can be used in templates
         */
        modelPackage = "Model";

        /**
         * Client Package.  Optional, if needed, this can be used in templates
         */
        clientPackage = "Client";

        /**
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        // TODO needed by migrated templates but perhaps shoudl just be default and only support .NET STandard 2.0
        additionalProperties.put("supportsAsync", true);
        // project instantiated
        //shared
        additionalProperties.put("projectGuidShared", projectGuidShared);
        // library
        additionalProperties.put("projectGuidLibrary", projectGuidLibrary);

        additionalProperties.put("emitDefaultValue", optionalEmitDefaultValue);
        additionalProperties.put("backslash", "\\");

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));

        regexModifiers = new HashMap<Character, String>();
        regexModifiers.put('i', "IgnoreCase");
        regexModifiers.put('m', "Multiline");
        regexModifiers.put('s', "Singleline");
        regexModifiers.put('x', "IgnorePatternWhitespace");

        // avoid clashes with known method names used in the refit implementation
        reservedWords.add("ToString");
        reservedWordsMappings.put("ToString", "ToStringValue");
        reservedWords.add("ToJson");
        reservedWordsMappings.put("ToJson", "ToJsonValue");
        reservedWords.add("Equals");
        reservedWordsMappings.put("Equals", "EqualsValue");
        reservedWords.add("GetHashCode");
        reservedWordsMappings.put("GetHashCode", "GetHashCodeValue");
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reserved words
     *
     * @return the escaped term
     */
    //@Override
    //public String escapeReservedWord(String name) {
    //    return "_" + name;  // add an underscore to the name
    //}

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + packageName + ".Shared" + File.separator + apiPackage();
    }

    /**
     * Location to write model files.  You can use the modelPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String modelFileFolder() {
        return outputFolder + File.separator + sourceFolder + File.separator + packageName + ".Shared" + File.separator + modelPackage();
    }

    @Override
    public String getArgumentsLocation() {
        return null;
    }

    //@Override
    //protected String getTemplateDir() {
    //  return templateDir;
    //}

    //@Override
    //public String getDefaultTemplateDir() {
    //  return templateDir;
    //}

    @Override
    public void processOpts() {
        super.processOpts();

        // {{packageName}}
        if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
            setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
        } else {
            additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
        }

        String packageFolder = sourceFolder + File.separator + packageName;
        String sharedFolder = packageFolder + ".Shared";
        String clientPackageDir = sharedFolder + File.separator + clientPackage;

        additionalProperties.put("packageFolder", packageFolder);
        additionalProperties.put("sharedFolder", sharedFolder);


        supportingFiles.add(new SupportingFile("Solution.mustache", "", packageName + ".sln"));
        supportingFiles.add(new SupportingFile("shared_project.mustache", sharedFolder, packageName + ".shared.shproj"));
        supportingFiles.add(new SupportingFile("shared_projitems.mustache", sharedFolder, packageName + ".shared.projitems"));
        supportingFiles.add(new SupportingFile("Project.mustache", packageFolder, packageName + ".csproj"));

        supportingFiles.add(new SupportingFile("ApiClient.mustache",
                clientPackageDir, "ApiClient.cs"));
    }
/*
    public String getSwaggerTypeX(Schema p) {
        String datatype = null;
        if (p instanceof StringSchema && "number".equals(p.getFormat())) {
            datatype = "BigDecimal";
        } else if (p instanceof ByteArraySchema) {
            datatype = "ByteArray";
        } else if (p instanceof BinarySchema) {
            datatype = "binary";
        } else if (p instanceof FileSchema) {
            datatype = "file";
        } else if (p instanceof BooleanSchema) {
            datatype = "boolean";
        } else if (p instanceof DateSchema) {
            datatype = "date";
        } else if (p instanceof DateTimeSchema) {
            datatype = "DateTime";
        } else if (p instanceof IntegerSchema) {
            datatype = "integer";
        } else if (p instanceof MapSchema) {
            datatype = "map";
        } else if (p instanceof NumberSchema) {
            datatype = "number";
        } else if (p instanceof UUIDSchema) {
            datatype = "UUID";
        /*} else if(p instanceof RefSchema) {
            try {
                RefSchema r = (RefSchema)p;
                datatype = r.get$ref();
                if(datatype.indexOf("#/definitions/") == 0) {
                    datatype = datatype.substring("#/definitions/".length());
                }
            } catch (Exception var4) {
                LOGGER.warn("Error obtaining the datatype from RefSchema:" + p + ". Datatype default to Object");
                datatype = "Object";
                LOGGER.error(var4.getMessage(), var4);
            }* /
        } else if (p instanceof StringSchema) {
            datatype = "string";
        } else if (p != null) {
            datatype = p.getType();
        }

        return datatype;
    }

    public String getSwaggerType(Schema p) {
        String swaggerType = getSwaggerTypeX(p);
        String type;
        if (typeMapping.containsKey(swaggerType.toLowerCase())) {
            type = typeMapping.get(swaggerType.toLowerCase());
            if (languageSpecificPrimitives.contains(type)) {
                return type;
            }
        } else {
            type = swaggerType;
        }
        return toModelName(type);
    }*/

    /*
    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }
*/
    /*
    @Override
    public String toModelName(String name) {
        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        name = sanitizeName(name);

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            //LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. return => ModelReturn (after camelize)
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            //LOGGER.warn(name + " (model name starts with number) cannot be used as model name. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. 200Response => Model200Response (after camelize)
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }*/

    /*@Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name);

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize the variable name
        // pet_id => PetId
        //name = camelize(name);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // sanitize name
        name = sanitizeName(name);

        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_");

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize(lower) the variable name
        // pet_id => petId
        //name = camelize(name, true);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }*/

    /*
    @Override
    public String getTypeDeclaration(Schema p) {
        if (p instanceof ArraySchema) {
            ArraySchema ap = (ArraySchema) p;
            Schema inner = ap.getItems();
            return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapSchema) {
            MapSchema mp = (MapSchema) p;
            Schema inner = (Schema) mp.getAdditionalProperties();

            return getSwaggerType(p) + "<string, " + getTypeDeclaration(inner) + ">";
        }
        return super.getTypeDeclaration(p);
    }*/

    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        if (propertySchema instanceof ArraySchema) {
            return getListTypeDeclaration(propertySchema);
        } else if (propertySchema instanceof MapSchema && hasSchemaProperties(propertySchema)) {
            return GetDictionaryTypeDeclaration(propertySchema);
        }

        String base = super.getTypeDeclaration(propertySchema);
        // fallback object type is dictionary, if we get that then we need to work out what kind of dictionary it should be
        if(base != null && base.equals("Dictionary")) {
            return GetDictionaryTypeDeclaration(propertySchema);
        }
        return base;
    }

    private String getListTypeDeclaration(Schema propertySchema) {
        Schema inner = ((ArraySchema) propertySchema).getItems();
        return String.format("%s<%s>", getSchemaType(propertySchema), getTypeDeclaration(inner));
    }

    private String GetDictionaryTypeDeclaration(Schema propertySchema) {
        Schema inner = (Schema) propertySchema.getAdditionalProperties();
        return String.format("%s<string, %s>", getSchemaType(propertySchema), getTypeDeclaration(inner));
    }

    /*
    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        super.postProcessOperations(objs);
        if (objs != null) {
            Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
            if (operations != null) {
                List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
                for (CodegenOperation operation : ops) {

                    // Check return types for collection
                    if (operation.returnType != null) {
                        String typeMapping;
                        int namespaceEnd = operation.returnType.lastIndexOf(".");
                        if (namespaceEnd > 0) {
                            typeMapping = operation.returnType.substring(namespaceEnd);
                        } else {
                            typeMapping = operation.returnType;
                        }

                        if (operation.returnType.contains("_"))
                            operation.returnType = toModelName(operation.returnType);

                        if (this.collectionTypes.contains(typeMapping)) {
                            operation.returnContainer = operation.returnType;

                        } else {
                            operation.returnContainer = operation.returnType;
                        }
                    }

                    processOperation(operation);

                }
            }
        }

        return objs;
    }*/

    @Override
    protected void processOperation(CodegenOperation operation) {
        operation.httpMethod = camelize(operation.httpMethod.toLowerCase());

        for (CodegenParameter parameter : operation.allParams) {
            if (parameter.description == null) {
                continue;
            }

            // TODO remove line feeds because we can't handle multiline comments in a single string in templates
            parameter.description = parameter.description.replaceAll("\\r|\\r\\n|\\n", " ");
        }

        if(operation.getHasBodyParam()){
            // check for bodyparams without datatypes, thanks Jira swagger
            if(operation.bodyParam.dataType == null || operation.bodyParam.dataType.equals("")) {
                operation.bodyParam.dataType = getTypeDeclaration(new StringSchema());
            }
        }
    }

    @Override
    public CodegenModel fromModel(String name, Schema schema, Map<String, Schema> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, schema, allDefinitions);
        if (allDefinitions != null && codegenModel != null && codegenModel.parent != null) {
            final Schema parentModel = allDefinitions.get(toModelName(codegenModel.parent));
            if (parentModel != null) {
                final CodegenModel parentCodegenModel = super.fromModel(codegenModel.parent, parentModel);
                boolean hasEnums = getBooleanValue(codegenModel, HAS_ENUMS_EXT_NAME);
                if (hasEnums) {
                    codegenModel = this.reconcileInlineEnums(codegenModel, parentCodegenModel);
                }

                Map<String, CodegenProperty> propertyHash = new HashMap<>(codegenModel.vars.size());
                for (final CodegenProperty property : codegenModel.vars) {
                    propertyHash.put(property.name, property);
                }

                for (final CodegenProperty property : codegenModel.readWriteVars) {
                    if (property.defaultValue == null && property.baseName.equals(parentCodegenModel.discriminator)) {
                        property.defaultValue = "\"" + name + "\"";
                    }
                }

                CodegenProperty last = null;
                for (final CodegenProperty property : parentCodegenModel.vars) {
                    // helper list of parentVars simplifies templating
                    if (!propertyHash.containsKey(property.name)) {
                        final CodegenProperty parentVar = property.clone();
                        parentVar.getVendorExtensions().put(CodegenConstants.IS_INHERITED_EXT_NAME, Boolean.TRUE);
                        parentVar.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.TRUE);
                        last = parentVar;
                        LOGGER.info("adding parent variable {}", property.name);
                        codegenModel.parentVars.add(parentVar);
                    }
                }

                if (last != null) {
                    last.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, Boolean.FALSE);
                }
            }
        }

        // Cleanup possible duplicates. Currently, readWriteVars can contain the same property twice. May or may not be isolated to C#.
        if (codegenModel != null && codegenModel.readWriteVars != null && codegenModel.readWriteVars.size() > 1) {
            int length = codegenModel.readWriteVars.size() - 1;
            for (int i = length; i >= 0; i--) {
                final CodegenProperty codegenProperty = codegenModel.readWriteVars.get(i);
                // If the property at current index is found earlier in the list, remove this last instance.
                for (int j=0; j < i; j++){
                    CodegenProperty cp = codegenModel.readWriteVars.get(j);
                    if(codegenProperty.equals(cp)) {
                        codegenModel.readWriteVars.remove(i);
                        break;
                    } else if (codegenProperty.name.equals(cp.name) || codegenProperty.name.equals( "_" + cp.name)
                            || ("_" + codegenProperty.name).equals( cp.name)) {
                        codegenProperty.name = codegenProperty.name + i;
                    }
                }
            }
        }

        return codegenModel;
    }

    private CodegenModel reconcileInlineEnums(CodegenModel codegenModel, CodegenModel parentCodegenModel) {
        // This generator uses inline classes to define enums, which breaks when
        // dealing with models that have subTypes. To clean this up, we will analyze
        // the parent and child models, look for enums that match, and remove
        // them from the child models and leave them in the parent.
        // Because the child models extend the parents, the enums will be available via the parent.

        // Only bother with reconciliation if the parent model has enums.
        boolean hasEnums = getBooleanValue(parentCodegenModel, HAS_ENUMS_EXT_NAME);
        if (hasEnums) {

            // Get the properties for the parent and child models
            final List<CodegenProperty> parentModelCodegenProperties = parentCodegenModel.vars;
            List<CodegenProperty> codegenProperties = codegenModel.vars;

            // Iterate over all of the parent model properties
            boolean removedChildEnum = false;
            for (CodegenProperty parentModelCodegenPropery : parentModelCodegenProperties) {
                // Look for enums
                boolean isEnum = getBooleanValue(parentModelCodegenPropery, IS_ENUM_EXT_NAME);
                if (isEnum) {
                    // Now that we have found an enum in the parent class,
                    // and search the child class for the same enum.
                    Iterator<CodegenProperty> iterator = codegenProperties.iterator();
                    while (iterator.hasNext()) {
                        CodegenProperty codegenProperty = iterator.next();
                        isEnum = getBooleanValue(codegenProperty, IS_ENUM_EXT_NAME);
                        if (isEnum && codegenProperty.equals(parentModelCodegenPropery)) {
                            // We found an enum in the child class that is
                            // a duplicate of the one in the parent, so remove it.
                            iterator.remove();
                            removedChildEnum = true;
                        }
                    }
                }
            }

            if (removedChildEnum) {
                // If we removed an entry from this model's vars, we need to ensure hasMore is updated
                int count = 0, numVars = codegenProperties.size();
                for (CodegenProperty codegenProperty : codegenProperties) {
                    count += 1;
                    codegenProperty.getVendorExtensions().put(CodegenConstants.HAS_MORE_EXT_NAME, count < numVars);
                }
                codegenModel.vars = codegenProperties;
            }
        }

        return codegenModel;
    }
    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty (should not occur as an auto-generated method name will be used)
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method name (operationId) not allowed");
        }

        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            //LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + camelize(sanitizeName("call_" + operationId)));
            operationId = "call_" + operationId;
        }

        return camelize(sanitizeName(operationId));
    }

        @Override
    public void postProcessParameter(CodegenParameter parameter) {
        postProcessPattern(parameter.pattern, parameter.vendorExtensions);
        super.postProcessParameter(parameter);
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        postProcessPattern(property.pattern, property.vendorExtensions);

        // check for properties without datatypes, thanks Jira swagger
        if (property.datatype == null || property.datatype.equals("")) {
            property.datatype = getTypeDeclaration(new StringSchema());
            property.datatypeWithEnum = getTypeDeclaration(new StringSchema());
            property.defaultValue = toDefaultValue(new StringSchema());
        }

        if (property.datatype != null && property.datatype.contains("Dictionary<string, >")){
            property.datatype = property.datatype.replace("Dictionary<string, >","Dictionary<string, string>");
        }

        if (property.datatypeWithEnum != null && property.datatypeWithEnum.contains("Dictionary<string, >")){
            property.datatypeWithEnum = property.datatypeWithEnum.replace("Dictionary<string, >","Dictionary<string, string>");
        }

        if (property.defaultValue != null && property.defaultValue.contains("Dictionary<string, >")){
            property.defaultValue = property.defaultValue.replace("Dictionary<string, >","Dictionary<string, string>");
        }

        if (property.datatype != null && property.datatype.contains("List<>")){
            property.datatype = property.datatype.replace("List<>","List<string>");
        }

        if (property.datatypeWithEnum != null && property.datatypeWithEnum.contains("List<>")){
            property.datatypeWithEnum = property.datatypeWithEnum.replace("List<>","List<string>");
        }

        if (property.defaultValue != null && property.defaultValue.contains("List<>")){
            property.defaultValue = property.defaultValue.replace("List<>","List<string>");
        }

        super.postProcessModelProperty(model, property);
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if ("int?".equalsIgnoreCase(datatype) || "long?".equalsIgnoreCase(datatype) ||
                "double?".equalsIgnoreCase(datatype) || "float?".equalsIgnoreCase(datatype)) {
            return value;
        } else {
            return "\"" + escapeText(value) + "\"";
        }
    }

    @Override
    public String toEnumVarName(String value, String datatype) {
        if (value.length() == 0) {
            return "Empty";
        }

        // for symbol, e.g. $, #
        if (getSymbolName(value) != null) {
            return camelize(getSymbolName(value));
        }

        // number
        if ("int?".equals(datatype) || "long?".equals(datatype) ||
                "double?".equals(datatype) || "float?".equals(datatype)) {
            String varName = "NUMBER_" + value;
            varName = varName.replaceAll("-", "MINUS_");
            varName = varName.replaceAll("\\+", "PLUS_");
            varName = varName.replaceAll("\\.", "_DOT_");
            return varName;
        }

        // string
        String var = value.replaceAll("_", " ");
        //var = WordUtils.capitalizeFully(var);
        var = camelize(var);
        var = var.replaceAll("\\W+", "");

        if (var.matches("\\d.*")) {
            return "_" + var;
        } else {
            return var;
        }
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setProjectGuidLibrary(String projectGuidLibrary) {
        this.projectGuidLibrary = projectGuidLibrary;
    }

    public void setProjectGuidShared(String projectGuidShared) {
        this.projectGuidShared = projectGuidShared;
    }

    protected void addOption(String key, String description, String defaultValue) {
        CliOption option = new CliOption(key, description);
        if (defaultValue != null) option.defaultValue(defaultValue);
        cliOptions.add(option);
    }


    /*
      * The swagger pattern spec follows the Perl convention and style of modifiers. .NET
      * does not support this syntax directly so we need to convert the pattern to a .NET compatible
      * format and apply modifiers in a compatible way.
      * See https://msdn.microsoft.com/en-us/library/yd1hzczs(v=vs.110).aspx for .NET options.
      * See https://github.com/swagger-api/swagger-codegen/pull/2794 for Python's initial implementation from which this is copied.
      */
    public void postProcessPattern(String pattern, Map<String, Object> vendorExtensions) {
        if (pattern != null) {
            int i = pattern.lastIndexOf('/');

            //Must follow Perl /pattern/modifiers convention
            if (pattern.charAt(0) != '/' || i < 2) {
                throw new IllegalArgumentException("Pattern must follow the Perl "
                        + "/pattern/modifiers convention. " + pattern + " is not valid.");
            }

            String regex = pattern.substring(1, i).replace("'", "\'");
            List<String> modifiers = new ArrayList<String>();

            // perl requires an explicit modifier to be culture specific and .NET is the reverse.
            modifiers.add("CultureInvariant");

            for (char c : pattern.substring(i).toCharArray()) {
                if (regexModifiers.containsKey(c)) {
                    String modifier = regexModifiers.get(c);
                    modifiers.add(modifier);
                } else if (c == 'l') {
                    modifiers.remove("CultureInvariant");
                }
            }

            vendorExtensions.put("x-regex", regex);
            vendorExtensions.put("x-modifiers", modifiers);
        }


    }

    /**
     * Return the default value of the property
     *
     * @param p Swagger property object
     * @return string presentation of the default value of the property
     */
    @Override
    public String toDefaultValue(Schema p) {
        if (p instanceof StringSchema) {
            StringSchema dp = (StringSchema) p;
            if (dp.getDefault() != null) {
                String _default = dp.getDefault();
                if (dp.getEnum() == null) {
                    return "\"" + _default + "\"";
                } else {
                    // convert to enum var name later in postProcessModels
                    return _default;
                }
            }
        } else if (p instanceof BooleanSchema) {
            BooleanSchema dp = (BooleanSchema) p;
            if (dp.getDefault() != null) {
                return dp.getDefault().toString();
            }
        } else if (p instanceof DateSchema) {
            // TODO
        } else if (p instanceof DateTimeSchema) {
            // TODO
        } else if (p instanceof IntegerSchema) {
            IntegerSchema dp = (IntegerSchema) p;
            if (dp.getDefault() != null) {
                return dp.getDefault().toString();
            }
        } else if (p instanceof NumberSchema) {
            NumberSchema dp = (NumberSchema) p;
            if (dp.getDefault() != null) {
                return dp.getDefault().toString();
            }
        }

        return null;
    }

}