package code.uz.bankcard.repository;

import code.uz.bankcard.dto.filter.CardFilterDTO;
import code.uz.bankcard.dto.filter.FilterResultDTO;
import code.uz.bankcard.entity.CardEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FilterRepository {
    private final EntityManager entityManager;

    public FilterResultDTO<CardEntity> filter(CardFilterDTO filterDTO, UUID profileId, boolean isAdmin, int page, int size) {
        StringBuilder visibleQuery = new StringBuilder(" where c.visible = true ");
        Map<String, Object> params = new HashMap<>();

        if (!isAdmin) {
            visibleQuery.append(" and c.profile.id = :profileId ");
            params.put("profileId", profileId);
        }

        if (filterDTO.getCardNumber() != null) {
            visibleQuery.append(" and c.cardNumber like :cardNumber ");
            params.put("cardNumber", "%" + filterDTO.getCardNumber() + "%");
        }

        Query selectQuery = entityManager.createQuery("select c from CardEntity c " + visibleQuery + " order by c.createdDate desc ");
        selectQuery.setFirstResult((page) * size);
        selectQuery.setMaxResults(size);
        params.forEach(selectQuery::setParameter);

        List<CardEntity> resultList = selectQuery.getResultList();

        String countQuery = "select count(c) from CardEntity c " + visibleQuery;
        Query count = entityManager.createQuery(countQuery);
        params.forEach(count::setParameter);
        Long totalElements = (Long) count.getSingleResult();

        return new FilterResultDTO<>(resultList, totalElements);
    }
}
