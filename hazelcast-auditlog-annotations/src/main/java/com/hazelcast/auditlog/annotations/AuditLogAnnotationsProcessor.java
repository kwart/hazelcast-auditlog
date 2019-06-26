package com.hazelcast.auditlog.annotations;

import static java.lang.Math.abs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.hazelcast.auditlog.AuditLogUtils;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

@MetaInfServices(Processor.class)
public class AuditLogAnnotationsProcessor extends AbstractProcessor {

    public static final int CODE_LENGTH = 6;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Set<Integer> codes = new HashSet<>();
        Elements elements = processingEnv.getElementUtils();

        for (Element element : roundEnv.getElementsAnnotatedWith(AuditMessages.class)) {
            AuditMessages annotation = element.getAnnotation(AuditMessages.class);
            if (annotation == null || element.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString();
            Builder classBuilder = TypeSpec.classBuilder(className + AuditLogUtils.GENERATED_CLASS_NAME_SUFFIX)
                    .addModifiers(Modifier.PUBLIC).addSuperinterface(TypeName.get(typeElement.asType()))
                    .addField(FieldSpec.builder(Logger.class, "LOGGER")
                            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$T.getFormatterLogger($S)", LogManager.class, "com.hazelcast.auditlog").build());
            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.METHOD) {
                    ExecutableElement executable = (ExecutableElement) enclosed;
                    Message msg = executable.getAnnotation(Message.class);
                    if (msg == null) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Method is missing @Message annotation: " + executable);
                        continue;
                    }
                    if (!codes.add(msg.code())) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                "Duplicate code (" + msg.code() + ") detected in @Message annotation for method: " + executable);
                        continue;
                    }
                    MethodSpec.Builder methodBuilder = MethodSpec.overriding(executable);
                    List<Object> args = new ArrayList<>();
                    StringBuilder sb = new StringBuilder("LOGGER.log($T.INFO, $S");
                    args.add(Level.class);
                    args.add(annotation.prefix() + "-" + zeroLeftPadding(msg.code()) + ": " + msg.value());
                    for (VariableElement varEl : executable.getParameters()) {
                        sb.append(", $L");
                        args.add(varEl.getSimpleName());
                        ;
                    }
                    sb.append(")");
                    methodBuilder.addStatement(sb.toString(), args.toArray());
                    classBuilder.addMethod(methodBuilder.build());
                }
            }
            JavaFile javaFile = JavaFile
                    .builder(elements.getPackageOf(typeElement).getQualifiedName().toString(), classBuilder.build()).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return false;
    }

    private String zeroLeftPadding(int code) {
        return String.format("%" + CODE_LENGTH + "d", abs(code)).replace(' ', '0');
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> supportedTypes = new LinkedHashSet<String>();
        supportedTypes.add(AuditMessages.class.getCanonicalName());
        supportedTypes.add(Message.class.getCanonicalName());
        return supportedTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
