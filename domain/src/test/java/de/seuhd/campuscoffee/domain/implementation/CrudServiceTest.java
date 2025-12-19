package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;
import de.seuhd.campuscoffee.domain.model.objects.DomainModel;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CrudServiceTest {

    private CrudDataService<TestDomain, Long> dataService;
    private TestCrudService crudService;

    @BeforeEach
    void setup() {
        dataService = mock(CrudDataService.class);
        crudService = new TestCrudService(dataService);
    }

    @Test
    void clearDelegatesToDataService() {
        crudService.clear();
        verify(dataService).clear();
    }

    @Test
    void getAllReturnsAllEntities() {
        List<TestDomain> domains = List.of(new TestDomain(1L));
        when(dataService.getAll()).thenReturn(domains);

        List<TestDomain> result = crudService.getAll();

        assertThat(result).isEqualTo(domains);
        verify(dataService).getAll();
    }

    @Test
    void getByIdReturnsEntity() {
        TestDomain domain = new TestDomain(1L);
        when(dataService.getById(1L)).thenReturn(domain);

        TestDomain result = crudService.getById(1L);

        assertThat(result).isEqualTo(domain);
        verify(dataService).getById(1L);
    }

    @Test
    void upsertCreatesNewEntityWhenIdIsNull() {
        TestDomain domain = new TestDomain(null);
        TestDomain saved = new TestDomain(1L);

        when(dataService.upsert(domain)).thenReturn(saved);

        TestDomain result = crudService.upsert(domain);

        assertThat(result.getId()).isEqualTo(1L);
        verify(dataService).upsert(domain);
    }

    @Test
    void upsertUpdatesExistingEntity() {
        TestDomain domain = new TestDomain(1L);

        when(dataService.getById(1L)).thenReturn(domain);
        when(dataService.upsert(domain)).thenReturn(domain);

        TestDomain result = crudService.upsert(domain);

        assertThat(result).isEqualTo(domain);
        verify(dataService).getById(1L);
        verify(dataService).upsert(domain);
    }

    @Test
    void upsertThrowsIfEntityDoesNotExist() {
        TestDomain domain = new TestDomain(1L);

        when(dataService.getById(1L))
                .thenThrow(new NotFoundException(TestDomain.class, 1L));

        assertThrows(NotFoundException.class, () -> crudService.upsert(domain));
        verify(dataService).getById(1L);
    }

    @Test
    void upsertThrowsDuplicationException() {
        TestDomain domain = new TestDomain(1L);

        when(dataService.getById(1L)).thenReturn(domain);
        when(dataService.upsert(domain))
                .thenThrow(new DuplicationException(TestDomain.class, "id", "1"));

        assertThrows(DuplicationException.class, () -> crudService.upsert(domain));
    }

    @Test
    void deleteDelegatesToDataService() {
        crudService.delete(1L);
        verify(dataService).delete(1L);
    }

    // ----------------------------------------------------
    // Helper classes
    // ----------------------------------------------------

    static class TestCrudService extends CrudServiceImpl<TestDomain, Long> {

        private final CrudDataService<TestDomain, Long> dataService;

        TestCrudService(CrudDataService<TestDomain, Long> dataService) {
            super(TestDomain.class);
            this.dataService = dataService;
        }

        @Override
        protected CrudDataService<TestDomain, Long> dataService() {
            return dataService;
        }
    }

    static class TestDomain implements DomainModel<Long> {

        private final Long id;

        TestDomain(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }
}
