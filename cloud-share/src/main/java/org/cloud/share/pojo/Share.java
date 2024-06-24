package org.cloud.share.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Share {
    private Integer id;// ID
    @NotEmpty
    private String fileName;// 文件名称
    private Integer createUser;//创建人ID
    private LocalDateTime createTime;//创建时间
    @NotEmpty
    private String serverFileName;// 重命名后的文件名称
    private String userName;// 用户名称

}
