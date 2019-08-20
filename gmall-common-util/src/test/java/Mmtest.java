import org.junit.Test;

import java.util.Arrays;

public class Mmtest {

      @Test//简单选择排序
      public void test3(){
          int [] a = {1,2,5,6,8,7,4};
          for (int i = 0; i <a.length ; i++) {
               int min = i;
              for (int j = i+1; j <a.length ; j++) {
                  if(a[j]<a[i]){
                      min=j;
                  }
                  if(min!=i){
                      int temp = a[i];
                      a[i] = a[j];
                      a[j] = temp;
                  }
              }
          }
          System.out.println(Arrays.toString(a));

    }

    public static void quickMethod(int [] a ){
        for (int i = 0; i < a.length; i++) {
            int min = i;
            //选出之后待排序中值最小的位置
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            //最小值不等于当前值时进行交换
            if (min != i) {
                int temp = a[i];
                a[i] = a[min];
                a[min] = temp;
            }
        }

    }
      @Test
    //java中排序 --冒泡
    public void test2(){
        int [] array = {1,2,5,6,8,7,4};
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j <array.length-i-1 ; j++) {
                if(array[j]>array[j+1]){
                    int temp = array[j];
                     array[j]=array[j+1];
                     array[j+1] =temp;
                }

            }

        }
        System.out.println(Arrays.toString(array));
    }
     @Test
    public void test1(){
         Integer f1=1,f2=1;
         Integer f3=150,f4=150;
         System.out.println(f1==f2);
         System.out.println(f3==f4);


     }

}
