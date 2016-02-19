package entity;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import config.Config;
import config.Strings;
import org.apache.http.util.TextUtils;
import utils.CheckUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zzz40500 on 2015/7/15.
 */
public class InnerClassEntity extends FieldEntity {


    private String packName;
    private String className;
    private List<? extends FieldEntity> fields;

    private String autoCreateClassName;

    public String getAutoCreateClassName() {
        return autoCreateClassName;
    }

    public void setAutoCreateClassName(String autoCreateClassName) {
        this.autoCreateClassName = autoCreateClassName;
    }

    public <T extends FieldEntity> void setFields(List<T> fields) {
        this.fields = fields;
    }

    private PsiClass psiClass;


    public String getRealType() {
        return String.format(super.getType(), className);
    }

    public void checkAndSetType(String s) {

        String regex = getType().replaceAll("%s", "(\\\\w+)").replaceAll("\\.", "\\\\.");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find() && matcher.groupCount() > 0) {

            String temp=matcher.group(1);
            if(TextUtils.isEmpty(temp)){
                setClassName(getAutoCreateClassName());
            }else{
                setClassName(matcher.group(1));
            }

        }

    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<? extends FieldEntity> getFields() {
        return fields;
    }


    public PsiClass getPsiClass() {
        return psiClass;
    }

    public void setPsiClass(PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    public String getFiledPackName() {


        String fullClassName;
        if (!TextUtils.isEmpty(getPackName())) {
            fullClassName = getPackName() + "." + getClassName();
        } else {
            fullClassName = getClassName();
        }

        return String.format(getType(), fullClassName);
    }


    public void generateFiled(PsiElementFactory mFactory, PsiClass mClass) {


        try {
            if (Config.getInstant().getAnnotationStr().equals(Strings.fastAnnotation)) {
                mClass.addBefore(mFactory.createAnnotationFromText("@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)", mClass), mClass);
            }
        } catch (Throwable e) {

        }


        if (Config.getInstant().isObjectFromData()) {
            createMethod(mFactory, Config.getInstant().getObjectFromDataStr().replace("$ClassName$", mClass.getName()).trim(), mClass);
        }
        if (Config.getInstant().isObjectFromData1()) {
            createMethod(mFactory, Config.getInstant().getObjectFromDataStr1().replace("$ClassName$", mClass.getName()).trim(), mClass);

        }
        if (Config.getInstant().isArrayFromData()) {
            createMethod(mFactory, Config.getInstant().getArrayFromDataStr().replace("$ClassName$", mClass.getName()).trim(), mClass);

        }
        if (Config.getInstant().isArrayFromData1()) {
            createMethod(mFactory, Config.getInstant().getArrayFromData1Str().replace("$ClassName$", mClass.getName()).trim(), mClass);
        }

        for (FieldEntity fieldEntity : getFields()) {
            if (fieldEntity instanceof InnerClassEntity) {


                ((InnerClassEntity) fieldEntity).generateSupperFiled(mFactory, mClass);
                ((InnerClassEntity) fieldEntity).generateClass(mFactory, mClass);
            } else {
                fieldEntity.generateFiled(mFactory, mClass);
            }
        }

        if (Config.getInstant().isFieldPrivateMode()) {
            createSetMethod(mFactory, getFields(), mClass);
            createGetMethod(mFactory, getFields(), mClass);
        }

    }

    public void generateClass(PsiElementFactory mFactory, PsiClass parentClass) {

        if (isGenerate()) {
            String classContent =
                    "public static class " + className + "{}";
            PsiClass subClass = mFactory.createClassFromText(classContent, null).getInnerClasses()[0];

            if (Config.getInstant().isObjectFromData()) {
                createMethod(mFactory, Config.getInstant().getObjectFromDataStr().replace("$ClassName$", subClass.getName()).trim(), subClass);
            }
            if (Config.getInstant().isObjectFromData1()) {
                createMethod(mFactory, Config.getInstant().getObjectFromDataStr1().replace("$ClassName$", subClass.getName()).trim(), subClass);
            }
            if (Config.getInstant().isArrayFromData()) {
                createMethod(mFactory, Config.getInstant().getArrayFromDataStr().replace("$ClassName$", subClass.getName()).trim(), subClass);

            }
            if (Config.getInstant().isArrayFromData1()) {
                createMethod(mFactory, Config.getInstant().getArrayFromData1Str().replace("$ClassName$", subClass.getName()).trim(), subClass);

            }


            for (FieldEntity fieldEntity : getFields()) {

                if (fieldEntity instanceof InnerClassEntity) {

                    ((InnerClassEntity) fieldEntity).generateSupperFiled(mFactory, subClass);
                    ((InnerClassEntity) fieldEntity).setPackName(getFiledPackName());

                    ((InnerClassEntity) fieldEntity).generateClass(mFactory, subClass);
                } else {

                    fieldEntity.generateFiled(mFactory, subClass);
                }
            }
            if (Config.getInstant().isFieldPrivateMode()) {
                createSetMethod(mFactory, getFields(), subClass);
                createGetMethod(mFactory, getFields(), subClass);
            }
            parentClass.add(subClass);
//            if (Config.getInstant().getAnnotationStr().equals(Strings.fastAnnotation)) {
//                subClass.addBefore(mFactory.createAnnotationFromText("@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)", subClass), subClass);
//            }
        }
    }

    public void generateSupperFiled(PsiElementFactory mFactory, PsiClass prentClass) {

        if (isGenerate()) {

            StringBuilder filedSb = new StringBuilder();
            String filedName = null;
            if (CheckUtil.getInstant().checkKeyWord(getFieldName())) {
                filedName = getFieldName() + "X";
            } else {
                filedName = getFieldName();
            }
            if (!TextUtils.isEmpty(getExtra())) {
                filedSb.append(getExtra()).append("\n");
            }
            if (!filedName.equals(getKey()) || Config.getInstant().isUseSerializedName()) {

                filedSb.append(Config.getInstant().geFullNametAnnotation().replaceAll("\\{filed\\}", getKey()));
//                filedSb.append("@com.google.gson.annotations.SerializedName(\"").append(getKey()).append("\")\n");
            }

            if (Config.getInstant().isFieldPrivateMode()) {

                filedSb.append("private  ");

            } else {
                filedSb.append("public  ");
            }

            filedSb.append(getFiledPackName()).append(" ").append(filedName).append(" ; ");
            prentClass.add(mFactory.createFieldFromText(filedSb.toString(), prentClass));
        }
    }

    private void createMethod(PsiElementFactory mFactory, String method, PsiClass cla) {
        cla.add(mFactory.createMethodFromText(method, cla));
    }

    private void createSetMethod(PsiElementFactory mFactory, List<? extends FieldEntity> fields, PsiClass mClass) {

        for (FieldEntity field1 : fields) {
            if (field1.isGenerate()) {
                String field = field1.getFieldName();
                String typeStr = field1.getRealType();

                if(Config.getInstant().isUseFiledNamePrefix()){
                  String   temp=field.replaceAll("^"+Config.getInstant().getFiledNamePreFixStr(),"");
                    if(!TextUtils.isEmpty(temp)){
                        field=temp;
                    }

                }

                String method = "public void  set" + captureName(field) + "( " + typeStr + " " + field + ") {   this." + field1.getFieldName() + " = " + field + ";} ";
                mClass.add(mFactory.createMethodFromText(method, mClass));
            }
        }

    }

    public String captureName(String name) {

        if (name.length() > 0) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;
    }


    private void createGetMethod(PsiElementFactory mFactory, List<? extends FieldEntity> fields, PsiClass mClass) {

        for (FieldEntity field1 : fields) {
            if (field1.isGenerate()) {
                String field = field1.getFieldName();

                String typeStr = field1.getRealType();
                if(Config.getInstant().isUseFiledNamePrefix()){
                    String   temp=field.replaceAll("^"+Config.getInstant().getFiledNamePreFixStr(),"");
                    if(!TextUtils.isEmpty(temp)){
                        field=temp;
                    }

                }


                if (typeStr.equals(" boolean ")) {

                    String method = "public " + typeStr + "   is" + captureName(field) + "() {   return " + field1.getFieldName() + " ;} ";
                    mClass.add(mFactory.createMethodFromText(method, mClass));
                } else {

                    String method = "public " + typeStr + "   get" + captureName(field) + "() {   return " + field1.getFieldName() + " ;} ";
                    mClass.add(mFactory.createMethodFromText(method, mClass));
                }
            }


        }

    }

    public static void main(String[] args) {
        String s = "dfdfd";


        Pattern pattern = Pattern.compile("(\\w+)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find() && matcher.groupCount() > 0) {

            System.out.print(matcher.group(1));
            System.out.print(matcher.groupCount());
        }
    }


}
