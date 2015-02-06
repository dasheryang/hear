package hear.app.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ArrayUtils {

    public static <T,E> void  each(List<T> array,Processor<T,E> p){
        if(isEmpty(array)){
            return;
        }
        if(p==null){
            return;
        }
        for(int i=0;i<array.size();i++){
            p.process(array.get(i));
        }
    }

    /**
     * map
     * @param array
     * @param transformer
     * @param <T>
     * @param <E>
     * @return
     */
    public static <T,E> List<E> map(List<T> array,Processor<T,E> transformer){
        if(array==null){
            return null;
        }
        List<E> result=new ArrayList<E>();
        for(int i=0;i<array.size();i++){
            result.add(transformer.process(array.get(i)));
        }
        return result;
    }

    /*
     * public interface UnaryPredicate<T> { public boolean test(T item); }
     */

    /*
     * public static <T> List<T> select(Collection<T> source, UnaryPredicate<T>
     * selector){ List<T> result = new ArrayList<T>();; for(T item : source){
     * if( selector.test( item )){ result.add(item); } } return result; }
     */

    public static <T> int find(T[] array, T target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 返回是不是为空
     * @param array
     * @return
     */
    public static <T> boolean isEmpty(List<T> array){
        return array==null || array.size()==0;
    }

    public static <T> boolean isEmpty(T[] array){
        return array==null || array.length==0;
    }

    /**
     * 从数组/原生数组中查找目标，对象使用equals方法，其它使用==号
     *
     * @param array
     * @param target
     * @return
     */
    public static <T> int find(List<T> array, T target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }



    public static int find(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static int find(int[] array, long target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 把数组对象join成一个字符串，调用toString方法
     *
     * @param list
     * @param sp
     * @return
     */
    public static <T> String join(List<T> list, String sp) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int iLen = list.size();
        int iLastIdx = iLen - 1;
        for (int i = 0; i < iLen; i++) {
            sb.append(list.get(i).toString());
            if (i != iLastIdx) {
                sb.append(sp);
            }
        }
        return sb.toString();
    }

    /**
     * 把数组对象join成一个字符串，调用toString方法
     *
     * @param list
     * @param sp
     * @return
     */
    public static <T> String join(List<T> list, String sp,String fieldsName) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int iLen = list.size();
        int iLastIdx = iLen - 1;
        for (int i = 0; i < iLen; i++) {
            T obj=list.get(i);
            Field f=null;
            if(fieldsName!=null){
                try{
                    f=obj.getClass().getField(fieldsName);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(f!=null){
                try{
                    f.setAccessible(true);
                    sb.append(f.get(obj));
                    f.setAccessible(false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else{
                sb.append(obj.toString());
            }
            if (i != iLastIdx) {
                sb.append(sp);
            }
        }
        return sb.toString();
    }

    public static <T> T findFirst(List<T> array, EqualeOP<T> cmp) {
        if (cmp == null) {
            return null;
        }
        for (int i = 0; i < array.size(); i++) {
            if (cmp.test(array.get(i),i)) {
                return array.get(i);
            }
        }
        return null;
    }

    public static <T> T findLast(List<T> array, EqualeOP<T> cmp) {
        if (cmp == null) {
            return null;
        }
        for (int i = array.size()-1; i >= 0; i--) {
            if (cmp.test(array.get(i),i)) {
                return array.get(i);
            }
        }
        return null;
    }

    /**
     * 从list中过滤出通过cmp.test方法为true的内容，生成另外一个数组
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> filter(List<T> list, EqualeOP<T> cmp) {
        if (list == null || list.size() == 0) {
            return list;
        }
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < list.size(); i++) {
            if (cmp.test(list.get(i),i)) {
                result.add(list.get(i));
            }
        }
        return result;
    }

    /**
     * 从list中去除通过cmp.test方法为true的内容，在原来数组上面操作
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> remove(List<T> list, EqualeOP<T> cmp) {
        if (list == null || list.size() == 0) {
            return list;
        }
        for(int i=list.size()-1;i>=0;i--){
            T object=list.get(i);
            if (cmp.test(object,i)) {
                list.remove(i);
            }
        }
        /*
        for (Iterator<T> i = list.iterator(); i.hasNext();) {
            T object = i.next();
            if (cmp.test(object,0)) {
                i.remove();
            }
        }
        */
        return list;
    }

    /**
     * 从list中保留调用cmp.test方法为true的item，在原来数组上面操作
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> keep(List<T> list, EqualeOP<T> cmp) {
        if (list == null || list.size() == 0) {
            return list;
        }
        for(int i=list.size()-1;i>=0;i--){
            T object=list.get(i);
            if (!cmp.test(object,i)) {
                list.remove(i);
            }
        }
        /*
        for (Iterator<T> i = list.iterator(); i.hasNext();) {
            T object = i.next();
            if (!cmp.test(object)) {
                i.remove();
            }
        }
        */
        return list;
    }


    /**
     * 倒序
     *
     * @param list
     * @param cmp
     * @return
     */
    public static <T> List<T> reverse(List<T> list) {
        if (list == null || list.size() == 0) {
            return list;
        }
        int lastIndex=list.size()-1;
        for(int i=0;i<list.size()/2;i++){
            T t=list.get(i);
            T temp=list.get(lastIndex-i);
            list.set(i, temp);
            list.set(lastIndex-i, t);
        }
        return list;
    }


    /**
     * 排序一个list数组
     *
     * @param list
     * @param cmp
     */
    public static <T> void sort(List<T> list, Comparator<T> cmp) {
        if (list == null || list.size() == 0) {
            return;
        }
        T[] newlist = (T[])list.toArray();
        Arrays.sort(newlist, cmp);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, newlist[i]);
        }
    }

    /**
     * 从原生的一个数组转换为list
     *
     * @param src
     * @return
     */
    public static <T> ArrayList<T> from(T[] src) {
        ArrayList<T> t = new ArrayList<T>();
        if(src!=null){
            for (int i = 0; i < src.length; i++) {
                t.add(src[i]);
            }
        }
        return t;
    }

    public static int size(List unreadMsg) {
        if(unreadMsg==null){
            return 0;
        }
        return unreadMsg.size();
    }

    public static abstract class EqualeOP<T> {
        public boolean test(T src,int index){
            return test(src);
        }
        public boolean test(T src){
            return false;
        }
    }

    public static interface Processor<T,E> {
        public E process(T src);
    }


    public static void main(String[] argv){
        String[] testString=new String[]{
                "a","b","c","d"
        };
        List<String> t =from(testString);
        reverse(t);
        System.out.println(join(t, "|"));
    }

}
