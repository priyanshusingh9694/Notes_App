package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Todo type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Todos", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
})
public final class Todo implements Model {
  public static final QueryField ID = field("Todo", "id");
  public static final QueryField TITLE = field("Todo", "title");
  public static final QueryField CONTENT = field("Todo", "content");
  public static final QueryField DATE = field("Todo", "date");
  public static final QueryField COLOR = field("Todo", "color");
  public static final QueryField IMAGE_PATH = field("Todo", "imagePath");
  public static final QueryField NEWID = field("Todo", "newid");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String title;
  private final @ModelField(targetType="String") String content;
  private final @ModelField(targetType="String") String date;
  private final @ModelField(targetType="Int") Integer color;
  private final @ModelField(targetType="String") String imagePath;
  private final @ModelField(targetType="Int") Integer newid;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String resolveIdentifier() {
    return id;
  }
  
  public String getId() {
      return id;
  }
  
  public String getTitle() {
      return title;
  }
  
  public String getContent() {
      return content;
  }
  
  public String getDate() {
      return date;
  }
  
  public Integer getColor() {
      return color;
  }
  
  public String getImagePath() {
      return imagePath;
  }
  
  public Integer getNewid() {
      return newid;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Todo(String id, String title, String content, String date, Integer color, String imagePath, Integer newid) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.date = date;
    this.color = color;
    this.imagePath = imagePath;
    this.newid = newid;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Todo todo = (Todo) obj;
      return ObjectsCompat.equals(getId(), todo.getId()) &&
              ObjectsCompat.equals(getTitle(), todo.getTitle()) &&
              ObjectsCompat.equals(getContent(), todo.getContent()) &&
              ObjectsCompat.equals(getDate(), todo.getDate()) &&
              ObjectsCompat.equals(getColor(), todo.getColor()) &&
              ObjectsCompat.equals(getImagePath(), todo.getImagePath()) &&
              ObjectsCompat.equals(getNewid(), todo.getNewid()) &&
              ObjectsCompat.equals(getCreatedAt(), todo.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), todo.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getContent())
      .append(getDate())
      .append(getColor())
      .append(getImagePath())
      .append(getNewid())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Todo {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("content=" + String.valueOf(getContent()) + ", ")
      .append("date=" + String.valueOf(getDate()) + ", ")
      .append("color=" + String.valueOf(getColor()) + ", ")
      .append("imagePath=" + String.valueOf(getImagePath()) + ", ")
      .append("newid=" + String.valueOf(getNewid()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static TitleStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Todo justId(String id) {
    return new Todo(
      id,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      content,
      date,
      color,
      imagePath,
      newid);
  }
  public interface TitleStep {
    BuildStep title(String title);
  }
  

  public interface BuildStep {
    Todo build();
    BuildStep id(String id);
    BuildStep content(String content);
    BuildStep date(String date);
    BuildStep color(Integer color);
    BuildStep imagePath(String imagePath);
    BuildStep newid(Integer newid);
  }
  

  public static class Builder implements TitleStep, BuildStep {
    private String id;
    private String title;
    private String content;
    private String date;
    private Integer color;
    private String imagePath;
    private Integer newid;
    @Override
     public Todo build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Todo(
          id,
          title,
          content,
          date,
          color,
          imagePath,
          newid);
    }
    
    @Override
     public BuildStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep content(String content) {
        this.content = content;
        return this;
    }
    
    @Override
     public BuildStep date(String date) {
        this.date = date;
        return this;
    }
    
    @Override
     public BuildStep color(Integer color) {
        this.color = color;
        return this;
    }
    
    @Override
     public BuildStep imagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }
    
    @Override
     public BuildStep newid(Integer newid) {
        this.newid = newid;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String title, String content, String date, Integer color, String imagePath, Integer newid) {
      super.id(id);
      super.title(title)
        .content(content)
        .date(date)
        .color(color)
        .imagePath(imagePath)
        .newid(newid);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder content(String content) {
      return (CopyOfBuilder) super.content(content);
    }
    
    @Override
     public CopyOfBuilder date(String date) {
      return (CopyOfBuilder) super.date(date);
    }
    
    @Override
     public CopyOfBuilder color(Integer color) {
      return (CopyOfBuilder) super.color(color);
    }
    
    @Override
     public CopyOfBuilder imagePath(String imagePath) {
      return (CopyOfBuilder) super.imagePath(imagePath);
    }
    
    @Override
     public CopyOfBuilder newid(Integer newid) {
      return (CopyOfBuilder) super.newid(newid);
    }
  }
  
}
