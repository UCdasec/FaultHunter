package com.afivd.afivd;

import java.util.ArrayList;
import java.util.List;

public class VariableSearcher extends CBaseListener{
    private ArrayList<VariableTuple> variables = new ArrayList<>(); // Name, Type

    // Will make a simple tuple class
    public class VariableTuple{
        private final String variableName;
        private final String variableType;
        private final int depth;
        public VariableTuple(String variableName, String variableType, int depth){
            this.variableName = variableName;
            this.variableType = variableType;
            this.depth = depth;
        }
        public String getVariableName() {return variableName;}
        public String getVariableType() {return variableType;}
        public int getDepth() {return depth;}
    }

    // ------------------------------------------ Listener Overrides ---------------------------------------------------

    // TODO: Wish I didn't have to copy and paste the same code for both listeners, but I cannot use the superclass to get the contents
    //  available to both contexts.

    // TODO: Add to both: record the function that the variable is in and modify variable tuple

    @Override
    public void enterForDeclaration(CParser.ForDeclarationContext ctx){
        // Collect all variables and store them in a Tuple
        String tempVarType=null;
        String tempVarName=null;

        //----- Case with no initializer and normal variable declaration Ex: "double d;"
        if(ctx.declarationSpecifiers() != null && ctx.initDeclaratorList() == null){
            List<CParser.DeclarationSpecifierContext> declarationSpecifiers = ctx.declarationSpecifiers().declarationSpecifier();
            List<CParser.TypeSpecifierContext> typeSpecifiers = new ArrayList<>();

            for (CParser.DeclarationSpecifierContext declarationSpecifier : declarationSpecifiers) {
                if (declarationSpecifier.typeSpecifier() != null) {
                    // Ignore prefixes to the type, will have to now distinguish between primitive types and defined types
                    // If it is a primitive type it will not have typeDefName. Defined types have only typeDefName for both
                    // the type and name of the variable, so we will have to distinguish by counting the number of typeDefName.
                    typeSpecifiers.add(declarationSpecifier.typeSpecifier());
                }
            }
            // Count number of typeDefNames, 1 == primitive declaration, 2 == defined declaration
            int numTypeDefNames = 0;
            for(CParser.TypeSpecifierContext typeSpecifierContext : typeSpecifiers){
                if(typeSpecifierContext.typedefName() != null){
                    numTypeDefNames++;
                }
            }

            // Pretty sure that it cannot be more than two entries for this case
            if(numTypeDefNames==1){
                tempVarType = typeSpecifiers.get(0).getText();
                tempVarName = typeSpecifiers.get(1).typedefName().getText();
            }else if(numTypeDefNames==2){
                tempVarType = typeSpecifiers.get(0).typedefName().getText();
                tempVarName = typeSpecifiers.get(1).typedefName().getText();
            }
            if(tempVarName != null && tempVarType != null){
                variables.add(new VariableTuple(tempVarName,tempVarType,ctx.depth()));
                typeSpecifiers.get(0).typedefName().depth();
            }

            //----- Case where there may be initializers or there are multiple variables of the same type being declared
            // Ex: "char c, ch;"
            // Ex: "bool isTrue, isFalse;"
            // Ex: "double d = 2;"
        }else if(ctx.declarationSpecifiers() != null && ctx.initDeclaratorList() != null){
            // Need to find the right declarationSpecifier with the typeSpecifier in it, in the case of prefixes to the type
            List<CParser.DeclarationSpecifierContext> declarationSpecifiers = ctx.declarationSpecifiers().declarationSpecifier();
            if(declarationSpecifiers.size()==1){
                if(declarationSpecifiers.get(0).typeSpecifier().typedefName() != null){
                    tempVarType = declarationSpecifiers.get(0).typeSpecifier().typedefName().getText();
                }else{
                    tempVarType = declarationSpecifiers.get(0).typeSpecifier().getText();
                }

            }else{
                for(CParser.DeclarationSpecifierContext declarationSpecifierContext : declarationSpecifiers){
                    if(declarationSpecifierContext.typeSpecifier()!=null){
                        // Should only be one in the list, hopefully
                        if(declarationSpecifierContext.typeSpecifier().typedefName() != null){
                            tempVarType = declarationSpecifierContext.typeSpecifier().typedefName().getText();
                        }else{
                            tempVarType = declarationSpecifierContext.typeSpecifier().getText();
                        }
                    }
                }
            }
            // Now get the list of declared variables (which will be part of a initDeclaractorList)
            List<CParser.InitDeclaratorContext> initDeclarators = ctx.initDeclaratorList().initDeclarator();
            for(CParser.InitDeclaratorContext initDeclaratorContext : initDeclarators){
                if(initDeclaratorContext.declarator() != null && initDeclaratorContext.declarator().directDeclarator() != null){
                    variables.add(new VariableTuple(initDeclaratorContext.declarator().directDeclarator().getText(),tempVarType,ctx.depth()));
                }

            }

        } else {
            // Nothing for now, may need this section to figure out unhandled declaration blocks
        }
    }

    @Override
    public void enterDeclaration(CParser.DeclarationContext ctx) {
        // Collect all variables and store them in a HashMap
        String tempVarType=null;
        String tempVarName=null;

        //----- Case with no initializer and normal variable declaration Ex: "double d;"
        if(ctx.declarationSpecifiers() != null && ctx.initDeclaratorList() == null){
            List<CParser.DeclarationSpecifierContext> declarationSpecifiers = ctx.declarationSpecifiers().declarationSpecifier();
            List<CParser.TypeSpecifierContext> typeSpecifiers = new ArrayList<>();

            for (CParser.DeclarationSpecifierContext declarationSpecifier : declarationSpecifiers) {
                if (declarationSpecifier.typeSpecifier() != null) {
                    // Ignore prefixes to the type, will have to now distinguish between primitive types and defined types
                    // If it is a primitive type it will not have typeDefName. Defined types have only typeDefName for both
                    // the type and name of the variable, so we will have to distinguish by counting the number of typeDefName.
                    typeSpecifiers.add(declarationSpecifier.typeSpecifier());
                }
            }
            // Count number of typeDefNames, 1 == primitive declaration, 2 == defined declaration
            int numTypeDefNames = 0;
            for(CParser.TypeSpecifierContext typeSpecifierContext : typeSpecifiers){
                if(typeSpecifierContext.typedefName() != null){
                    numTypeDefNames++;
                }
            }

            // Pretty sure that it cannot be more than two entries for this case
            if(numTypeDefNames==1){
                tempVarType = typeSpecifiers.get(0).getText();
                tempVarName = typeSpecifiers.get(1).typedefName().getText();
            }else if(numTypeDefNames==2){
                tempVarType = typeSpecifiers.get(0).typedefName().getText();
                tempVarName = typeSpecifiers.get(1).typedefName().getText();
            }
            if(tempVarName != null && tempVarType != null){
                variables.add(new VariableTuple(tempVarName,tempVarType,ctx.depth()));
            }

        //----- Case where there may be initializers or there are multiple variables of the same type being declared
            // Ex: "char c, ch;"
            // Ex: "bool isTrue, isFalse;"
            // Ex: "double d = 2;"
        }else if(ctx.declarationSpecifiers() != null && ctx.initDeclaratorList() != null){
            // Need to find the right declarationSpecifier with the typeSpecifier in it, in the case of prefixes to the type
            List<CParser.DeclarationSpecifierContext> declarationSpecifiers = ctx.declarationSpecifiers().declarationSpecifier();
            if(declarationSpecifiers.size()==1){
                if(declarationSpecifiers.get(0).typeSpecifier().typedefName() != null){
                    tempVarType = declarationSpecifiers.get(0).typeSpecifier().typedefName().getText();
                }else{
                    tempVarType = declarationSpecifiers.get(0).typeSpecifier().getText();
                }

            }else{
                for(CParser.DeclarationSpecifierContext declarationSpecifierContext : declarationSpecifiers){
                    if(declarationSpecifierContext.typeSpecifier()!=null){
                        // Should only be one in the list, hopefully
                        if(declarationSpecifierContext.typeSpecifier().typedefName() != null){
                            tempVarType = declarationSpecifierContext.typeSpecifier().typedefName().getText();
                        }else{
                            tempVarType = declarationSpecifierContext.typeSpecifier().getText();
                        }
                    }
                }
            }
            // Now get the list of declared variables (which will be part of a initDeclaractorList)
            List<CParser.InitDeclaratorContext> initDeclarators = ctx.initDeclaratorList().initDeclarator();
            for(CParser.InitDeclaratorContext initDeclaratorContext : initDeclarators){
                if(initDeclaratorContext.declarator() != null && initDeclaratorContext.declarator().directDeclarator() != null){
                    variables.add(new VariableTuple(initDeclaratorContext.declarator().directDeclarator().getText(),tempVarType,ctx.depth()));
                }

            }

        } else {
            // Nothing for now, may need this section to figure out unhandled declaration blocks
        }
    }



    // -------------------------------------------- Helper Functions ---------------------------------------------------

    /**
     * GetVariables returns an ArrayList of VariableTuples for the parsed C file.
     * @return An ArrayList of VariableTuples. which contains the variable name and the type
     */
    public ArrayList<VariableTuple> getVariables() {
        return variables;
    }
}
