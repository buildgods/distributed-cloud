package org.cloud.file.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
    Integer id; // ID
    @NotEmpty
    String fileName;// 文件名称，多文件用&连接
    @NotNull
    private Integer categoryId;//文章分类id
    private Integer createUser;//创建人ID
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTime;//创建时间
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updateTime;//更新时间
    @NotEmpty
    String serverFileName;// 重命名后的文件名称，多文件用&连接
}
