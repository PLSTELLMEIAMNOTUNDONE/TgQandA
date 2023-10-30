package main.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import main.models.Question;
import main.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface  QuestionRepository extends JpaRepository<Question, Long> {



}
