package com.yyh.web.admin;

import com.yyh.domain.Blog;
import com.yyh.domain.User;
import com.yyh.service.IBlogService;
import com.yyh.service.ITagService;
import com.yyh.service.TypeService;
import com.yyh.vo.BlogQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.UUID;


@Controller
@RequestMapping("/admin")
public class BlogController {

    private static final String INPUT = "admin/blogs-input";
    private static final String LIST = "admin/blogs";
    private static final String REDIRECT_LIST = "redirect:/admin/blogs";

    @Autowired
    private IBlogService blogService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private ITagService tagService;

    @GetMapping("/blogs")
    public String blogs(@PageableDefault(size = 8,sort = {"updateTime"},direction = Sort.Direction.DESC) Pageable pageable,
                        BlogQuery blog, Model model){
        model.addAttribute("types",typeService.listType());
        model.addAttribute("page",blogService.listBlog(pageable,blog));
        return LIST;
    }

    @PostMapping("/blogs/search")
    public String search(@PageableDefault(size = 8,sort = {"updateTime"},direction = Sort.Direction.DESC) Pageable pageable,
                         BlogQuery blog, Model model){

        model.addAttribute("page",blogService.listBlog(pageable,blog));
        return "admin/blogs :: blogList";
    }

    @GetMapping("/blogs/input")
    public String input(Model model){
        setTypeAndTag(model);
        model.addAttribute("blog",new Blog());
        return INPUT;
    }

    @GetMapping("/blogs/{id}/input")
    public String editInput(@PathVariable long id, Model model){
        setTypeAndTag(model);
        Blog blog = blogService.getBlog(id);
        blog.init();
        model.addAttribute("blog",blog);
        return INPUT;
    }

    private void setTypeAndTag(Model model){
        model.addAttribute("types",typeService.listType());
        model.addAttribute("tags",tagService.listTag());
    }

    @PostMapping("/blogs")
    public String post(@RequestParam("fileUpload") MultipartFile fileUpload, Blog blog, RedirectAttributes attributes, HttpSession session){
        //??????????????????
        //???????????????
        String fileName = fileUpload.getOriginalFilename();
        if(!fileUpload.isEmpty()) {
            //?????????????????????
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            //?????????????????????
            fileName = UUID.randomUUID() + suffixName;
            //???blog??????????????????????????????
            blog.setFirstPicture("/images/" + fileName);
        }
        //????????????classes/static?????????
        String staticPath = ClassUtils.getDefaultClassLoader().getResource("static").getPath();
        //?????????????????????????????????
        String savePath = staticPath + File.separator + "images" + File.separator + fileName;

        //blog??????
        blog.setUser((User) session.getAttribute("user"));
        blog.setType(typeService.getType(blog.getType().getId()));
        blog.setTags(tagService.listTag(blog.getTagIds()));
        Blog b;
        if(blog.getId() == null){
            b = blogService.saveBlog(blog);
        }else {
            b = blogService.updateBlog(blog.getId(),blog);
        }
        //??????????????????????????????????????????
        try {
            //??????????????????static/images????????????  ?????????????????????
            if(!fileUpload.isEmpty()) {
                fileUpload.transferTo(new File(savePath));
            }
            if(b == null){
                attributes.addFlashAttribute("message","????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("message","????????????");
        }
        return REDIRECT_LIST;
    }

    @GetMapping("/blogs/{id}/delete")
    public String deleteBlog(@PathVariable Long id,RedirectAttributes attributes){
        blogService.deleteBlog(id);
        attributes.addFlashAttribute("message","????????????");
        return REDIRECT_LIST;
    }

}
