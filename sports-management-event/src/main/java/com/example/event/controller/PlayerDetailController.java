//package com.example.event.controller;
//
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Arrays;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/player")
//public class PlayerDetailController {
//
//    /**
//     * 获取客队球员详细数据
//     */
//    @GetMapping("/away-players")
//    public List<PlayerDetail> getAwayPlayers() {
//        return Arrays.asList(
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18)
//        );
//    }
//
//    /**
//     * 获取主队球员详细数据
//     */
//    @GetMapping("/home-players")
//    public List<PlayerDetail> getHomePlayers() {
//        return Arrays.asList(
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18),
//                new PlayerDetail("莫兰特", "34:54", 22, 3, 3, 9, 18)
//        );
//    }
//
//}