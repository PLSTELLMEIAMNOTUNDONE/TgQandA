package main.services;

import jakarta.persistence.*;
import main.logs.Logger;
import main.models.Photo;
import main.models.Question;
import main.models.Tag;
import main.repositories.PhotoRepository;
import main.repositories.QuestionRepository;
import main.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionService {
    @PersistenceContext
    EntityManager em;
    private final TagRepository tr;
    private final PhotoRepository pr;
    private final QuestionRepository qr;

    public QuestionService(@Autowired QuestionRepository qr, @Autowired TagRepository tr,
                           @Autowired PhotoRepository pr) {
        this.qr = qr;
        this.tr = tr;
        this.pr = pr;
    }


    public Question insertQuestion(Question question) {
        List<Question> list = getQuestionByText(question.getQuestion());
        if (list.isEmpty()) {
            qr.save(question);
            Logger.log(question, question, "insertQuestion");
            return question;
        } else {
            Logger.log(question, list.get(0), "insertQuestion");
            return list.get(0);
        }

    }

    public void updateQuestion(Question question) {
        if (qr.existsById(question.getId())) qr.save(question);
    }

    public void insertQuestion(Question question, String... tags) {
        question = insertQuestion(question);
        for (String tag : tags) {
            addTag(tag, question);
        }
    }


    public void addPhoto(Question question, String... photos) {
        Long id = question.getId();
        if (!qr.existsById(id)) {
            return;
        }

        for (String photo : photos) {
            Photo ph = new Photo();
            ph.setPath(photo);
            ph.setQ_id(id);
            pr.save(ph);
        }
        Logger.log("" + question.toString() + "\t " + Arrays.toString(photos), "", "addPhoto");

    }


    public void addTag(String text, Question question) {
        Long id = question.getId();
        if (!qr.existsById(id)) {
            return;
        }
        Tag tag = new Tag();


        tag.setTag(text);
        tag.setQ_id(id);

        tr.save(tag);


    }

    public void addTags(Question question, String... tags) {
        for (String tag : tags) {
            addTag(tag, question);
        }
    }

    public List<Question> getQuestionByText(String text) {
        TypedQuery<Question> query = em.createQuery(SELECT_QUESTION_BY_NAME, Question.class);
        query.setParameter(1, text);
        List<Question> list = query.getResultList();
        Logger.log(text, list, "getQuestionByText");
        return list;


    }


    public List<Tag> getTagsByText(List<String> tags) {
        List<Tag> list = new ArrayList<>();
        for (String tag : tags) {
            list.addAll(getTagsByText(tag));
        }
        return list;


    }

    public List<Tag> getTagsByText(String tag) {
        TypedQuery<Tag> query = em.createQuery(SELECT_TAG_BY_NAME, Tag.class);
        query.setParameter(1, tag);
        List<Tag> list = query.getResultList();
        Logger.log(tag, list, "getTagsByText");
        return list;


    }

    public List<Question> getQuestionsWithTag(Tag tag) {
        TypedQuery<Question> query = em.createQuery(SELECT_QUESTION_BY_TAG, Question.class);
        query.setParameter(1, tag.getQ_id());
        List<Question> list = query.getResultList();
        Logger.log(tag, list, "getQuestionsWithTag");
        return query.getResultList();
    }

    public HashSet<Question> getQuestionsWithTags(Tag... tags) {
        HashSet<Question> set = new HashSet<>();
        for (Tag tag : tags) {
            set.addAll(getQuestionsWithTag(tag));
        }
        return set;
    }

    public HashSet<Question> getQuestionsWithTags(List<Tag> tags) {
        HashSet<Question> set = new HashSet<>();
        for (Tag tag : tags) {
            set.addAll(getQuestionsWithTag(tag));
        }
        return set;
    }

    public List<Photo> getPhotoByQuestionId(Question question) {

        TypedQuery<Photo> query = em.createQuery(SELECT_PHOTO_BY_QUESTION_ID, Photo.class);
        query.setParameter(1, question.getId());
        List<Photo> list = query.getResultList();
        Logger.log(question, list, "getQuestionsWithTag");
        return query.getResultList();


    }

    public List<Tag> getTagByQuestionId(Question question) {
        TypedQuery<Tag> query = em.createQuery(SELECT_TAGS_BY_QUESTION_ID, Tag.class);
        query.setParameter(1, question.getId());
        List<Tag> list = query.getResultList();
        Logger.log(question, list, "getTagByQuestionId");
        return query.getResultList();
    }

    public List<Photo> getPhotoByQuestionId(List<Question> questions) {

        List<Photo> list = new ArrayList<>();
        for (Question question : questions) {
            list.addAll(getPhotoByQuestionId(question));
        }
        return list;


    }

    public Optional<Question> getQuestionById(Long id) {

        return qr.findById(id);

    }

    private final String SELECT_TAGS_BY_QUESTION_ID = "select t from Question q inner join Tag t on  t.q_id = ?1";
    private final String SELECT_QUESTION_BY_TAG = "select q from Question q inner join Tag t on  q.id = ?1";
    private final String SELECT_TAG_BY_NAME = "select t from Tag t where t.tag = ?1";
    private final String SELECT_PHOTO_BY_QUESTION_ID = "select p from Question q inner join Photo p on  p.q_id = ?1";
    private final String SELECT_QUESTION_BY_NAME = "select q from Question q where q.question = ?1";
}
