package ar.ppedemon.wta.service;

import ar.ppedemon.wta.data.ComparisonDao;
import ar.ppedemon.wta.model.Comparison;
import ar.ppedemon.wta.model.ComparisonResult;
import ar.ppedemon.wta.util.Base64Encoder;
import com.google.common.collect.Lists;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
@DisplayName("Dao-based comparison service")
class DaoComparisonServiceTest {

    private static final String CMP_ID = "123";
    private static final String USER_ID = UUID.randomUUID().toString();

    private ComparisonDao comparisonDao;
    private Comparator comparator;

    private Base64Encoder base64Encoder;
    private DaoComparisonService comparisonService;

    @BeforeEach
    private void init(Vertx vertx) {
        this.base64Encoder = new Base64Encoder();
        this.comparisonDao = mock(ComparisonDao.class);
        this.comparator = mock(Comparator.class);
        this.comparisonService = new DaoComparisonService(comparisonDao, comparator, vertx);
    }

    @Test
    @DisplayName("must correctly upsert left side of a comparison")
    void upsertLeft_always_mustInvokeDaoCorrectly() {
        comparisonService.upsertLeft(USER_ID, CMP_ID, base64Encoder.encode("Hi"));

        ArgumentCaptor<ComparisonDao.Side> captor = ArgumentCaptor.forClass(ComparisonDao.Side.class);
        verify(comparisonDao).upsertSide(anyString(), anyString(), captor.capture(), anyString());
        ComparisonDao.Side side = captor.getValue();

        assertThat(side, equalTo(ComparisonDao.Side.LEFT));
    }

    @Test
    @DisplayName("must correctly upsert right side of a comparison")
    void upsertRight_always_mustInvokeDaoCorrectly() {
        comparisonService.upsertRight(USER_ID, CMP_ID, base64Encoder.encode("Hi"));

        ArgumentCaptor<ComparisonDao.Side> captor = ArgumentCaptor.forClass(ComparisonDao.Side.class);
        verify(comparisonDao).upsertSide(anyString(), anyString(), captor.capture(), anyString());
        ComparisonDao.Side side = captor.getValue();

        assertThat(side, equalTo(ComparisonDao.Side.RIGHT));
    }

    @Test
    @DisplayName("return empty computation when comparing on non-existing comparison")
    void comparing_nonExistingComparison_mustReturnEmpty() {
        when(comparisonDao.get(anyString(), anyString())).thenReturn(Maybe.empty());
        Maybe<ResultWrapper<ComparisonResult>> result = comparisonService.compare(USER_ID, CMP_ID);
        result.test().assertNoValues();
    }

    @Test
    @DisplayName("return fail result when comparing on invalid comparison")
    void comparing_invalidComparison_mustReturnInvalidResult() {
        when(comparisonDao.get(anyString(), anyString()))
                .thenReturn(Maybe.just(new Comparison(CMP_ID, USER_ID).setLeft("abc")));

        Maybe<ResultWrapper<ComparisonResult>> result = comparisonService.compare(USER_ID, CMP_ID);
        result.test().assertValue(ResultWrapper::fail);
    }

    @Test
    @DisplayName("cached comparison must not invoke comparator")
    void comparing_whenCachedComparison_mustNotInvokeComparator() {
        when(comparisonDao.get(anyString(), anyString()))
                .thenReturn(Maybe.just(new Comparison(CMP_ID, USER_ID)
                        .setLeft("abc")
                        .setRight("abc")
                        .setResult(new ComparisonResult(ComparisonResult.Status.EQUAL, Lists.newArrayList()))));

        Maybe<ResultWrapper<ComparisonResult>> result = comparisonService.compare(USER_ID, CMP_ID);
        result.test()
                .assertValue(ResultWrapper::success)
                .assertValue(wrapper -> wrapper.result().isEqual());

        verify(comparator, never()).compare(any(Comparison.class));
    }

    @Test
    @DisplayName("non cached comparison must compute and update comparison result")
    void comparing_whenNonCachedComparison_mustComputeAndUpdateResult() {
        when(comparisonDao.get(anyString(), anyString()))
                .thenReturn(Maybe.just(new Comparison(CMP_ID, USER_ID)
                        .setLeft("abc")
                        .setRight("abc")));

        when(comparisonDao.updateResult(any(Comparison.class), any(ComparisonResult.class)))
                .thenReturn(Single.just(true));

        when(comparator.compare(any(Comparison.class)))
                .thenReturn(new ComparisonResult(ComparisonResult.Status.EQUAL, Lists.newArrayList()));

        Maybe<ResultWrapper<ComparisonResult>> result = comparisonService.compare(USER_ID, CMP_ID);
        result.test()
                //Wait for computation completion, since compare is called outside the event loop
                .awaitDone(5, TimeUnit.SECONDS)
                .assertValue(ResultWrapper::success)
                .assertValue(wrapper -> wrapper.result().isEqual());


        verify(comparator, times(1)).compare(any(Comparison.class));

        verify(comparisonDao, times(1)).updateResult(
                any(Comparison.class),
                any(ComparisonResult.class));
    }

    @Test
    @DisplayName("deletion must invoke correctly dao deletion")
    void deletion_always_mustInvokeDaoCorrectly() {
        comparisonService.delete(USER_ID, CMP_ID);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(comparisonDao).delete(captor.capture(), captor.capture());
        List<String> args = captor.getAllValues();

        Assertions.assertEquals(args.get(0), USER_ID);
        Assertions.assertEquals(args.get(1), CMP_ID);
    }
}
