package com.wz.xlinksnap.common.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * 描述: 多线程并发执行工具类 使用方法为
 *
 * @Autowired ConcurrentJobExecutor concurrentExecutor;
 */
@AllArgsConstructor
@Validated
public class ConcurrentJobExecutor {

    @NotNull
    private final Executor taskExecutor;

    /**
     * 包含了程序执行返回结果
     * 支持调用链
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, taskExecutor);
    }

    /**
     * 批量处理然后返回结果 支持调用链
     */
    public <T> List<T> supplyAsync(@NotNull Collection<Supplier<T>> suppliers) {
        CompletableFuture<T>[] ary = suppliers.stream().map(supplier -> CompletableFuture
                .supplyAsync(supplier, taskExecutor)).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(ary).join();
        return Arrays.stream(ary).map(CompletableFuture::join).collect(Collectors.toList());
    }

    /**
     * 支持带参数的批量处理,入参无需包装成List 支持调用链
     *
     * @param handler
     * @param params
     * @param <T>
     * @param <P>
     * @return
     */
    public <T, P> List<T> supplyAsync(@NotNull SupplierHandler<T, P> handler, @NotNull Collection<P> params) {
        CompletableFuture<T>[] ary = params.stream().map(param -> CompletableFuture
                        .supplyAsync(() -> handler.handle(param), taskExecutor))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(ary).join();
        return Arrays.stream(ary).map(CompletableFuture::join).collect(Collectors.toList());
    }

    /**
     * 不包含程序执行返回结果
     * 支持调用链
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, taskExecutor);
    }
}