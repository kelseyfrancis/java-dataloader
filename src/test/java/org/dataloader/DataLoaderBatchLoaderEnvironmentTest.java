package org.dataloader;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.dataloader.BatchLoaderEnvironment.newBatchLoaderEnvironment;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests related to context.  DataLoaderTest is getting to big and needs refactoring
 */
public class DataLoaderBatchLoaderEnvironmentTest {

    @Test
    public void context_is_passed_to_batch_loader_function() throws Exception {
        BatchLoader<String, String> batchLoader = new BatchLoader<String, String>() {
            @Override
            public CompletionStage<List<String>> load(List<String> keys) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletionStage<List<String>> load(List<String> keys, BatchLoaderEnvironment environment) {
                List<String> list = keys.stream().map(k -> k + "-" + environment.getContext()).collect(Collectors.toList());
                return CompletableFuture.completedFuture(list);
            }
        };
        DataLoaderOptions options = DataLoaderOptions.newOptions()
                .setBatchLoaderEnvironmentProvider(() -> newBatchLoaderEnvironment().context("ctx").build());
        DataLoader<String, String> loader = new DataLoader<>(batchLoader, options);

        loader.load("A");
        loader.load("B");
        loader.loadMany(asList("C", "D"));

        List<String> results = loader.dispatchAndJoin();

        assertThat(results, equalTo(asList("A-ctx", "B-ctx", "C-ctx", "D-ctx")));
    }

    @Test
    public void context_is_passed_to_map_batch_loader_function() throws Exception {
        MapBatchLoader<String, String> mapBatchLoader = (keys, environment) -> {
            Map<String, String> map = new HashMap<>();
            keys.forEach(k -> map.put(k, k + "-" + environment.getContext()));
            return CompletableFuture.completedFuture(map);
        };
        DataLoaderOptions options = DataLoaderOptions.newOptions()
                .setBatchLoaderEnvironmentProvider(() -> newBatchLoaderEnvironment().context("ctx").build());
        DataLoader<String, String> loader = new DataLoader<>(mapBatchLoader, options);

        loader.load("A");
        loader.load("B");
        loader.loadMany(asList("C", "D"));

        List<String> results = loader.dispatchAndJoin();

        assertThat(results, equalTo(asList("A-ctx", "B-ctx", "C-ctx", "D-ctx")));
    }

    @Test
    public void null_is_passed_as_context_if_you_do_nothing() throws Exception {
        BatchLoader<String, String> batchLoader = new BatchLoader<String, String>() {
            @Override
            public CompletionStage<List<String>> load(List<String> keys) {
                throw new UnsupportedOperationException("this wont be called");
            }

            @Override
            public CompletionStage<List<String>> load(List<String> keys, BatchLoaderEnvironment environment) {
                List<String> list = keys.stream().map(k -> k + "-" + environment.getContext()).collect(Collectors.toList());
                return CompletableFuture.completedFuture(list);
            }
        };
        DataLoader<String, String> loader = new DataLoader<>(batchLoader);

        loader.load("A");
        loader.load("B");
        loader.loadMany(asList("C", "D"));

        List<String> results = loader.dispatchAndJoin();

        assertThat(results, equalTo(asList("A-null", "B-null", "C-null", "D-null")));
    }

    @Test
    public void null_is_passed_as_context_to_map_loader_if_you_do_nothing() throws Exception {
        MapBatchLoader<String, String> mapBatchLoader = (keys, environment) -> {
            Map<String, String> map = new HashMap<>();
            keys.forEach(k -> map.put(k, k + "-" + environment.getContext()));
            return CompletableFuture.completedFuture(map);
        };
        DataLoader<String, String> loader = new DataLoader<>(mapBatchLoader);

        loader.load("A");
        loader.load("B");
        loader.loadMany(asList("C", "D"));

        List<String> results = loader.dispatchAndJoin();

        assertThat(results, equalTo(asList("A-null", "B-null", "C-null", "D-null")));
    }
}