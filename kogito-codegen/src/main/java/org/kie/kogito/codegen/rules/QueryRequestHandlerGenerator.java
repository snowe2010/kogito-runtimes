/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.rules;

import java.util.NoSuchElementException;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.modelcompiler.builder.QueryModel;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.kogito.codegen.context.KogitoBuildContext;

import static org.drools.modelcompiler.builder.generator.DrlxParseUtil.classNameToReferenceType;

public class QueryRequestHandlerGenerator extends QueryEndpointGenerator {

    public QueryRequestHandlerGenerator(RuleUnitDescription ruleUnit, QueryModel query, KogitoBuildContext context) {
        super(ruleUnit, query, context, "RequestHandlerQuery", "RequestHandler");
    }

    @Override
    protected void generateQueryMethods(CompilationUnit cu, ClassOrInterfaceDeclaration clazz, String returnType) {
        generateInterfaces(clazz, returnType);
        generateHandleRequestMethods(cu, clazz, returnType);
    }

    private void generateInterfaces(ClassOrInterfaceDeclaration clazz, String returnType) {
        ClassOrInterfaceType implementedTypes = clazz.getImplementedTypes(0);
        implementedTypes.asClassOrInterfaceType().setTypeArguments(classNameToReferenceType(ruleUnit.getCanonicalName()),
                                                                   classNameToReferenceType(returnType));
    }

    private void generateHandleRequestMethods(CompilationUnit cu, ClassOrInterfaceDeclaration clazz, String returnType) {
        MethodDeclaration handleRequestMethod = clazz.getMethodsByName("handleRequest").get(0);
        handleRequestMethod.getParameter(0).setType(ruleUnit.getCanonicalName() + (context.hasDI() ? "" : "DTO"));
        handleRequestMethod.setType(toNonPrimitiveType(returnType));

        Statement statement = handleRequestMethod
                .getBody()
                .orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"))
                .getStatement(0);
        statement.findAll(VariableDeclarator.class).forEach(decl -> setUnitGeneric(decl.getType()));
        statement.findAll(MethodCallExpr.class).forEach(m -> m.addArgument(context.hasDI() ? "unitDTO" : "unitDTO.get()"));

        Statement secondStatementListResults = handleRequestMethod
                .getBody()
                .orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"))
                .getStatement(1);
        secondStatementListResults.findAll(VariableDeclarator.class).forEach(decl -> setGeneric(decl.getType(), returnType));
        secondStatementListResults.findAll(ClassExpr.class).forEach(expr -> expr.setType(queryClassName));

        Statement returnMethodSingle = handleRequestMethod
                .getBody()
                .orElseThrow(() -> new NoSuchElementException("A method declaration doesn't contain a body!"))
                .getStatement(2);
        returnMethodSingle.findAll(VariableDeclarator.class).forEach(decl -> decl.setType(toNonPrimitiveType(returnType)));

        if (context.getAddonsConfig().useMonitoring()) {
            addMonitoringToResource(cu, new MethodDeclaration[]{handleRequestMethod}, endpointName);
        }
    }
}
