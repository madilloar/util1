package org.example;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

public class OptionalTest {
  @Test
  public void testOptional() {
    // Optionalを使った例
    java.util.Optional<String> optionalValue = java.util.Optional.of("Hello, World!");
    String value = optionalValue.orElse("Default Value");
    System.out.println(value); // "Hello, World!"と表示される
  }

  @Test
  public void test1() {
    // 1. Optional<Integer> a があるとき、a の値を二乗したい。
    // ただし、 a が empty の場合は empty を得たい。
    // 冗長すぎる。。。
    Optional<Integer> a = Optional.of(3);
    Optional<Integer> actual = a.isPresent() ? Optional.of(a.get() * a.get()) : Optional.empty();
    assertEquals(9, actual.get().intValue());

    // mapを使うと、もっと簡潔に書ける。
    // もし、aが empty でなければ Optionalに包まれた中の値を取り出し、
    // 取り出された値をxに渡す。
    // そして、x * x の結果を Optional に包んで返す。
    // もし、aが empty であれば、何もせずに empty を返す。
    // これが Optional の良いところ。
    assertEquals(Optional.of(9), a.map(x -> x * x));

    // aが empty であれば、何もせずに empty を返す。
    Optional<Integer> b = Optional.empty();
    assertEquals(Optional.empty(), b.map(x -> x * x));
  }

  // 2. Optional<Double> a があるとき、 a
  // の平方根を計算したい。ただし、 a が empty または負の数の場合は empty を得たい。
  // なお、 Math.sqrt を安全にした（負の数を渡すと
  // empty を返す）関数 Optional<Double> safeSqrt(double) があるものとして考えて良い。
  Function<Double, Optional<Double>> safeSqrt = (x) -> x < 0.0 ? Optional.empty() : Optional.of(Math.sqrt(x));

  @Test
  public void test2() {
    assertEquals(Optional.of(2.0), safeSqrt.apply(4.0));

    // 引数が負の数の場合は empty を返す。
    // 例えば、-4.0 の場合は empty を返す。
    // なお、Math.sqrt(-4.0) は NaN を返す。
    // これは、Math.sqrt の仕様である。
    // したがって、safeSqrt.apply(-4.0) は empty を返す。
    // なお、Optional.empty() は Optional<Double> 型である。
    // したがって、Optional.empty() は Optional<Double> 型である。
    // エラーを返すためにempty() を使っている。
    assertEquals(Optional.empty(), safeSqrt.apply(-4.0));
    assertEquals(Optional.of(0.0), safeSqrt.apply(0.0));

    // mapは、Optionalの中の値を取り出して、safeSqrtに渡す。
    // safeSqrtは「Optional<Double>」型を返すが、mapは更にそれをOptional型
    // に包むので、最終的に Optional<Optional<Double>> 型になる。
    assertEquals(Optional.of(Optional.of(2.0)), Optional.of(4.0).map(safeSqrt));

    // flatMapは、Optionalの中の値を取り出して、safeSqrtに渡す。
    // safeSqrtは「Optional<Double>」型を返すが、flatMapは更にそれをOptional型
    // に包まないので、最終的に Optional<Double> 型になる。
    // したがって、flatMapを使うと、Optional<Optional<Double>> 型にならない。
    assertEquals(Optional.of(2.0), Optional.of(4.0).flatMap(safeSqrt));

    assertEquals(Optional.empty(), Optional.of(-2.0).flatMap(safeSqrt));
  }

  @Test
  public void test3() {
    // flatMap(x -> x) は、flatMap(Function.identity()) と同じ。
    // これは、どちらもOptionalの中の値を取り出して、そのまま返す。
    Optional<Optional<Integer>> nested = Optional.of(Optional.of(3));
    assertEquals(Optional.of(3), nested.flatMap(x -> x));
    assertEquals(Optional.of(3), nested.flatMap(Function.identity()));
  }

  Function<Optional<Integer>, Function<Optional<Integer>, Optional<Integer>>> addNoGoodFunction1 = (a) -> (b) -> {
    if (a.isPresent()) {
      if (b.isPresent()) {
        return Optional.of(a.get() + b.get());
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  };

  Function<Optional<Integer>, Function<Optional<Integer>, Optional<Integer>>> addGoodFunction = a -> b -> a
      .flatMap(x -> b.flatMap(y -> Optional.of(x + y)));

  @Test
  public void test4() {
    Optional<Integer> a = Optional.of(3);
    Optional<Integer> b = Optional.of(4);
    assertEquals(Optional.of(7), addNoGoodFunction1.apply(a).apply(b));
    Optional<Integer> c = addGoodFunction.apply(a).apply(b);
    assertEquals(Optional.of(7), c);
    c.ifPresentOrElse(
        result -> System.out.println("Result: " + result),
        () -> System.out.println("Result is empty"));

    a = Optional.empty();
    b = Optional.of(4);
    assertEquals(Optional.empty(), addNoGoodFunction1.apply(a).apply(b));
    assertEquals(Optional.empty(), addGoodFunction.apply(a).apply(b));

    a = Optional.of(3);
    b = Optional.empty();
    assertEquals(Optional.empty(), addNoGoodFunction1.apply(a).apply(b));
    assertEquals(Optional.empty(), addGoodFunction.apply(a).apply(b));
  }

  /**
   * Math.pow(x, y) は、x の y 乗を計算する。
   * ただし、x が負の数の場合は NaN を返す。
   * なお、Math.pow(x, y) は、x の y 乗を計算する関数である。
   * ただし、x が負の数の場合は NaN を返す。
   * なお、safePow は、x の y 乗を計算する関数である。
   * ただし、x が負の数の場合は empty を返す。
   */
  Function<Double, Function<Double, Optional<Double>>> safePow = (
      x) -> (y) -> x < 0.0 ? Optional.empty() : Optional.of(Math.pow(x, y));

  /**
   * 処理が失敗する場合は一番内側をflatMapで包む。
   * そうしないと、Optional<Optional<Double>> 型になってしまう。
   */
  @Test
  public void test5() {
    // Math.pow(-1.0, 0.5) は NaN を返す。
    assertEquals(Double.NaN, Math.pow(-1.0, 0.5), 0.0);

    // safePowは、aが負の数の場合は empty を返すようにしている
    Optional<Double> a = Optional.of(-1.0);
    Optional<Double> b = Optional.of(0.5);
    Optional<Double> c = a.flatMap(x -> b.flatMap(y -> safePow.apply(x).apply(y)));
    assertEquals(Optional.empty(), c);
    c.ifPresentOrElse(
        result -> System.out.println("Result: " + result),
        () -> System.out.println("Result is empty"));

    Optional<Double> d = Optional.of(2.0);
    Optional<Double> e = Optional.of(3.0);
    Optional<Double> f = d.flatMap(x -> e.flatMap(y -> safePow.apply(x).apply(y)));
    assertEquals(Optional.of(8.0), f);
    f.ifPresentOrElse(
        result -> System.out.println("Result: " + result),
        () -> System.out.println("Result is empty"));
  }

  /**
   * コレクションとしてのOptional
   * Optionalはコレクションではないが、コレクションのように扱うことができる。
   * Optionalは要素がゼロ個か1個のコレクションである。
   * Optionalは、要素がゼロ個の場合は empty を返し、
   * 要素が1個の場合はその要素を返す。
   */
  @Test
  public void test6() {
    Optional<Integer> a = Optional.of(3);
    Optional<Integer> b = Optional.of(9);
    assertEquals(Optional.of(9), Optional.of(3).map(x -> x * x));

    Optional<Integer> c = Optional.empty();
    Optional<Integer> d = Optional.empty();
    assertEquals(d, c.map(x -> x * x));

    Optional<Integer> e = Optional.of(2);
    Optional<Integer> f = Optional.of(3);
    Optional<Integer> g = Optional.of(5);
    assertEquals(g, e.flatMap(x -> f.map(y -> x + y)));
  }

  /**
   * StreamとOptionalがよく似ているのがわかると思う。
   */
  @Test
  public void test7() {
    Stream<Integer> a = Stream.of(3);
    Stream<Integer> b = Stream.of(9);
    assertEquals(b.toList(), a.map(x -> x * x).toList());

    Stream<Integer> c = Stream.of();
    Stream<Integer> d = Stream.of();
    assertEquals(d.toList(), c.map(x -> x * x).toList());

    Stream<Integer> e = Stream.of(2);
    Stream<Integer> f = Stream.of(3);
    Stream<Integer> g = Stream.of(5);
    assertEquals(g.toList(),
        e.flatMap(x -> f.map(y -> x + y)).toList());
  }

  /**
   * filterは、Optionalの中の値を取り出して、そのまま返す。
   * ただし、filterの条件に合わない場合は empty を返す。
   * なお、filterの条件に合う場合は、そのまま返す。
   */
  @Test
  public void test8() {
    Stream<Integer> a = Arrays.asList(1, 2, 3, 4, 5).stream(); // [1, 2, 3, 4, 5]
    Stream<Integer> b = a.filter(x -> x % 2 == 0); // [2, 4]
    assertEquals(Stream.of(2, 4).toList(), b.toList());

    // [2]
    assertEquals(Stream.of(2).toList(), Stream.of(2).filter(x -> x % 2 == 0).toList());
    // []
    assertEquals(Stream.of().toList(), Stream.of(3).filter(x -> x % 2 == 0).toList());
    // []
    Stream<Integer> emptyStream = Stream.of();
    assertEquals(Stream.of().toList(), emptyStream.filter(x -> x % 2 == 0).toList());

    Optional<Integer> c = Optional.of(2); // [2] に相当
    Optional<Integer> d = c.filter(x -> x % 2 == 0); // Optional[2]
    assertEquals(Optional.of(2), d);

    Optional<Integer> e = Optional.of(3); // [3] に相当
    Optional<Integer> f = e.filter(x -> x % 2 == 0); // Optional.emptry()
    assertEquals(Optional.empty(), f);

    Optional<Integer> g = Optional.empty(); // [] に相当
    Optional<Integer> h = g.filter(x -> x % 2 == 0); // Optional.emptry()
    assertEquals(Optional.empty(), h);
  }

  @Test
  public void test9() {
    // CollectionのforEachは、ラムダ式とメソッド参照の両方が使える。
    // forEachは、コレクションの要素を1つずつ取り出して、ラムダ式やメソッド参照を実行する。
    List<String> a = Arrays.asList("a", "b", "c", "d", "e");
    System.out.print("ラムダ式:");
    a.forEach(x -> System.out.println(x));
    System.out.print("メソッド参照:");
    a.forEach(System.out::println);

    List<String> empty = Arrays.asList();
    System.out.print("empty:");
    System.out.print("[");
    empty.forEach(System.out::print);
    System.out.println("]");

    // OptionalはforEachはないがifPresentがある。
    // Optionalは要素がゼロ個か1個のコレクションである。
    // Optionalは、要素がゼロ個の場合は empty を返し、
    // 要素が1個の場合はその要素を返す。
    Optional<Integer> b = Optional.of(3);
    System.out.print("Optional:");
    b.ifPresent(x -> System.out.println(x)); // 3と表示される
    b.ifPresent(System.out::println); // 3と表示される

  }

  /**
   * モナド則。
   */
  @Test
  public void test10() {

    Function<Integer, Optional<Integer>> fFunction = x -> {
      System.out.println(x);
      return Optional.of(x);
    };
    Function<Integer, Optional<Integer>> gFunction = x -> {
      System.out.println(x);
      return Optional.of(x + 1);
    };

    // Foo.of(x).flatMap(x -> f(x)) == f(x) が成り立つ。
    assertEquals(Optional.of(3), Optional.of(3).flatMap(x -> fFunction.apply(x)));

    // foo.flatMap(x -> Foo.of(x)) == foo が成り立つ。
    Optional<Integer> foo = Optional.of(3);
    assertEquals(Optional.of(3), foo.flatMap(x -> Optional.of(x)));

    // foo.flatMap(x -> f(x).flatMap(y -> g(y))) ==
    // foo.flatMap(x -> f(x)).flatMap(y -> g(y)) が成り立つ。
    assertEquals(foo.flatMap(x -> fFunction.apply(x).flatMap(y -> gFunction.apply(y))),
        foo.flatMap(x -> fFunction.apply(x)).flatMap(y -> gFunction.apply(y)));
  }

}