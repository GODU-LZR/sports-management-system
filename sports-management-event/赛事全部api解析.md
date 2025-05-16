**注意：有关getXXXOptions的方法，全部移到最后的option的api里面解决**

建议后端建一个option的controller



### 页面：/sport/type(已有接口)

**1、获取带赛事列表的赛事项目**

```
export const getSport1 = () => {

返回数据：
data：[
{ sportId: 123451, name: "篮球", games: [{gameId: 123, name: 'NBA季后赛'}, {gameId: 123, name: 'CBA常规赛'}] },
{ sportId: 123452, name: "足球", games: [{gameId: 123, name: 'NBA季后赛'}, {gameId: 123, name: 'CBA常规赛'}] },
],
}
```



**2、获取不带赛事列表的赛事项目数据(不应该存在上面已有的赛事项目)**

```
export const getSport2 = () => {

返回数据：
data: [
{ sportId: 123451, name: "篮球"},
{ sportId: 123452, name: "足球"},
],
}
```



### 页面：/sport/competition/{sportId}(已有接口)

**1、获取指定赛事项目、经过过滤后的赛事的总条数**

**需修改：新增了过滤器参数，之前忘记了添加过滤器，导致只能统计全部赛事的数量**

```
export const getCompetitionPage = (sportId, filter) => {
参数：
1、sportId：'123' // 赛事项目id
2、filter(新增参数)：过滤器
      filter: {
        name: '春季联赛', // 赛事名称，建议使用模糊匹配
        state: 0, // 赛事状态，0-不可报名、1-可报名、2-未开始、3-正在举行、4-已结束。后端应该根据赛事的报名开始结束时间、赛事开始结束时间与当前时间进行比较，得出赛事状态
        registerTime: '2025-05-12 23:25', // 报名时间值，如果该值在赛事报名时间内即为符合条件。如果报名时间为null，则一律为不符合条件 
        time: '2025-05-12 23:25' // 举办时间值，如果该值在赛事举办时间内即为符合条件。如果赛事开始、借宿时间为null，则一律为不符合条件 
      }

返回数据：
data: 300(查询符合条件的赛事的数量)
}
```



**2、获取符合条件的、已通过审核的赛事列表，建议一次返回10条数据**

**！！！需修改：上次没有指明，这里返回赛事，都必须是已通过审核的赛事**

```
export const getCompetitionData = (sportId, page, filter) => {
参数：
1、sportId：'M123' // 赛事项目id
2、page：1 // 页码(从1开始，建议一次返回10条数据)
3、filter：过滤器
      filter: {
        name: '春季联赛', // 赛事名称，建议使用模糊匹配
        state: 0, // 赛事状态，0-不可报名、1-可报名、2-未开始、3-正在举行、4-已结束。后端应该根据赛事的报名开始结束时间、赛事开始结束时间与当前时间进行比较，得出赛事状态
        registerTime: '2025-05-12 23:25', // 报名时间值，如果该值在赛事报名时间内即为符合条件。如果报名时间为null，则一律为不符合条件 
        time: '2025-05-12 23:25' // 举办时间值，如果该值在赛事举办时间内即为符合条件。如果赛事开始、借宿时间为null，则一律为不符合条件 
      }

返回数据：
data: 
[
    {
        gameId: 1, // 赛事id
        name: '春季联赛', // 赛事名称
        sport: '篮球', // 赛事项目名称(无需id，因为仅做展示)
        responsiblePeople: '张三', // 负责人名称(无需id，因为仅做展示)
        phone: '12345678912', // 联系电话
        register_start_time: '2026-01-01 00:00', // 报名开始时间
        register_end_time: '2026-03-01 00:00', // 报名结束时间
        start_time: '2026-04-01 00:00', // 赛事举办开始时间
        end_time: '2026-05-01 00:00', // 赛事举办结束时间
        // mode: 1 // 匹配模式，0-系统按时间分配、1-系统按随机分配、2-用户自定义(无论有没有都可以，没有任何影响)
	},
    {
        gameId: 2,
        name: '秋季联赛',
        sport: '篮球',
        responsiblePeople: '李四',
        phone: '12345678912',
        register_start_time: '2023-01-01 00:00',
        register_end_time: '2026-03-01 00:00',
        start_time: '2026-04-01 00:00',
        end_time: '2026-05-01 00:00',
        // mode: 1
    },
]
}
```



### 页面：/sport/game/{gameId}(已有接口)

**1、获取赛事的基本信息**

**！！！需修改：上次没有指明，这里返回赛事，都必须是已通过审核的赛事**

```
export const getGameData = (gameId) => {
参数：
1、gameId：'G20231128002' // 赛事id

返回数据：
data: {
    gameId: 1, // 赛事id
    name: '春季联赛', // 赛事名称
    sport: '篮球', // 赛事项目名称(无需id，因为仅做展示)
    responsiblePeople: '张三', // 负责人名称(无需id，因为仅做展示)
    phone: '12345678912', // 联系电话
    register_start_time: '2026-01-01 00:00', // 报名开始时间
    register_end_time: '2026-03-01 00:00', // 报名结束时间
    start_time: '2026-04-01 00:00', // 赛事举办开始时间
    end_time: '', // 赛事举办结束时间
    note: '备注信息', 
    // mode: 1 // 匹配模式，0-系统按时间分配、1-系统按随机分配、2-用户自定义(无论有没有都可以，没有任何影响)
},
}
```



**2、获取赛事的对应比赛列表**

备注：比赛是否关注需要根据当前用户来判断。

```
export const getMatches = (gameId) => {
参数：
1、gameId：'G20231128002' // 赛事id

返回数据：
data：
[
    {
    matchId: 'M20231128001', //比赛id
    sport: "篮球", // 赛事项目名称
    awayTeam: '软件1223', // 客队名称
    homeTeam: '软件1224', // 主队名称
    awayTeam_score: 20, // 客队得分
    homeTeam_score: 10, // 主队得分
    venue_name: "篮球场1号", // 比赛场地
    start_time: "2025-01-03 14:00", // 比赛开始时间
    end_time: "2025-01-03 15:30", // 比赛结束时间
    is_followed: 1, // 该用户是否关注了该比赛。0-未关注，1-已关注
    phase: 1, // 赛段，取值为1~N。数字越大，则越接近决赛
    winner: "软件1224" // 获胜队伍名称。取值为：客队名称、主队名称、平局、待定
    },
    {
    matchId: 'M20231128002',
    sport: "羽毛球",
    awayTeam: '软件1223',
    homeTeam: '软件1224',
    awayTeam_score: 20,
    homeTeam_score: 10,
    venue_name: "羽毛球馆2号",
    start_time: "2025-01-03 10:00",
    end_time: "2025-01-03 11:30",
    is_followed: 0,
    phase: 1,
    winner: "软件1224"
    }
]
}
```



**3、用户关注或取消关注赛事**

备注：这个方法是将比赛的关注变为未关注、未关注变为关注。

```
export const handleFollow = (matchId) => {
参数：
matchId： 'M20231128002'// 比赛id

返回数据：
data：成功或失败的标志
}
```



### 页面：/sport/match/{matchId}(已有接口)

**1、获取比赛的基本信息数据**

```
// 获取比赛的基本信息数据
export const getMatchData = (matchId) => {
参数：
matchId: 'M20231128002' // 比赛id

返回数据：
matchData: {
    matchId: 'M20231128002', // 比赛id
    sport: '篮球', // 赛事项目名称(无需id，因为仅做展示)
    venue_name: '篮球馆1号', // 比赛场地
    referee_name: ['王裁判', '张裁判', '李裁判'], // 裁判名称数组(无需id，因为仅做展示)
    start_time: '2024-03-15 14:00', // 比赛开始时间
    end_time: '2024-03-15 16:00', // 比赛结束时间
    responsiblePerson: '张管理员', // 负责人名称
    phone: '13800138000', // 联系电话
    note: '请各队提前30分钟到场热身，迟到15分钟视为弃权', // 比赛备注信息
    is_followed: 1, // 该用户是否关注比赛。0-未关注、1-已关注
},
}
```



**2、获取比赛的球员得分(需创建：这个方法在上一次里没有给出)**

```
export const getPlayersData = (matchId) => {
参数：
matchId：'M20231128002' // 比赛id

返回值：
data：
[
	[	// 一定要将客队得分放到索引0的位置
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18}
      ],

      [	// 一定要将主队得分放到索引1的位置
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18},
        {name:"莫兰特" ,minutes:"34:54" ,pts:22 ,reb:3 ,ast:3 ,fgMade:9 ,fgAtt:18}
      ]
]
}
```







### 页面：/my/competition(已有接口)

**1、获取过滤后，"我的"赛事的总数目**

**！！！(需注意：review_status为字符串类型)**

备注：这个返回赛事列表里，一定要是本用户所创建的赛事，所以需要在userInfo里获取userId。

```
export const getCompetitionPage = (review_status) => {
参数：
review_status: "0"(字符串类型) // 审核状态。0-未审核、1-已通过、2-已否决、3-已撤销

}
```



**2、获取"我的"赛事基本信息数据**

**！！！(需修改：因为tab栏绑定数字0会有问题，需要修改审核状态值)**

备注：这个返回数据的一定要是本用户所创建的赛事，所以需要在userInfo里获取userId。

```
export const getCompetitionData = (page, review_status) => {
参数：
page：1 // 页码(从1开始，建议一次返回10条数据)
review_status: "0"(字符串形式) // 审核状态。0-未审核、1-已通过、2-已否决、3-已撤销

返回数据：
data：[
        {
          gameId: 1,
          name: '春季联赛',
          sport: '篮球',
          responsiblePeople: '张三',
          phone: '12345678912',
          register_start_time: '2026-01-01 00:00',
          register_end_time: '2026-03-01 00:00',
          start_time: '2026-04-01 00:00',
          end_time: '2026-05-01 00:00',
        },
        {
          gameId: 2,
          name: '秋季联赛',
          sport: '篮球',
          responsiblePeople: '李四',
          phone: '12345678912',
          register_start_time: '2023-01-01 00:00',
          register_end_time: '2023-03-01 00:00',
          start_time: '2026-04-01 00:00',
          end_time: '2026-05-01 00:00',
        },
      ],
}
```



### 页面：/my/game/{gameId}(已有接口)

1、根据gameId，获取我创建的指定赛事的基本信息

**！！！(需修改：返回值新增了一个reason属性，表示赛事被"否决"和"撤销"的原因。"待审核"和"已通过"的reason属性为空)**

**！！！(需修改：返回值新增了一个review_status属性，表示赛事的审核状态)**

备注：这个查询赛事基本信息接口：首先要求查询用户，是创建该赛事的用户。其次，任意审核状态都可以查询到

```
// 获取我的赛事的基本数据
export const getGameData = (gameId) => {
参数：
1、gameId: 'G20250513' // 赛事id

返回数据：
data: {
    gameId: 'G20250513', // 赛事id
    name: '春季联赛', // 赛事名称
    sport: '篮球', //赛事项目名称(无需id，因为用户无法修改)
    responsiblePeopleId: 需要负责人的用户id。因为需要修改
    responsiblePeople: '张三', // 负责人名称
    phone: '12345678912', // 联系电话
    register_start_time: '2026-01-01 00:00', // 赛事报名开始时间
    register_end_time: '2026-03-01 00:00', // 赛事报名结束时间
    start_time: '2026-04-01 00:00', // 赛事开始时间
    end_time: '2026-04-04 00:00', // 赛事结束时间
    note: '备注信息', // 备注信息
    mode: 2 // 匹配模式，这个必须加：0-系统按时间分配、1-系统按随机分配、2-用户自定义
    review_status: 0, // 0-待审核、1-已通过、2-已否决、3-已撤销
    reason: '否决原因' // 否决或撤销原因，"已通过"和"待审核"赛事，此属性为空
},
}
```



**2、获取我创建的赛事，该赛事内的比赛列表**

备注：同样的，这些比赛的所属赛事(即参数的gameId对应的赛事)，也必须是查询者所创建的

```
export const getMatches = (gameId) => {
参数：
1、gameId：'G20250513' // 赛事id

返回数据：
data：
[
    {
    matchId: 123456,
    sport: "篮球",
    awayTeam: '软件1223',
    homeTeam: '软件1224',
    venue_name: "篮球场1号",
    start_time: "2025-01-03 14:00",
    end_time: "2025-01-03 15:30",
    },
    {
    matchId: 123457,
    sport: "羽毛球",
    awayTeam: '软件1223',
    homeTeam: '软件1224',
    venue_name: "羽毛球馆2号",
    start_time: "2025-01-03 10:00",
    end_time: "2025-01-03 11:30",
    },
]


}
```



**3、修改赛事的基本信息数据**

备注：修改表单并不是表示每个属性都有实际上的修改。建议直接数据库无脑覆盖。

```
export const updateGameData = (form) => {
参数：
1、form： // 修改表单
        form：{
        gameId: 1, // 需要修改的赛事id
        name: '春季联赛', // 修改后的赛事名称
        responsiblePersonId: '123', // 修改后的负责人userId
        phone: '12345678912', // 修改后的联系电话
        register_start_time: '2026-01-01 00:00', // 修改后的赛事报名开始时间
        register_end_time: '2026-03-01 00:00', // 修改后的赛事报名结束时间
        start_time: '2026-04-01 00:00', // 修改后的赛事开始时间
        end_time: '', // 修改后的赛事结束时间
        note: '备注信息', // 修改后的备注信息
        mode: 2 // 修改后的匹配模式
     },
}
```



**4、创建者为自己的赛事，添加新的比赛(需创建：这个方法在上一次里没有给出)**

```
export const addMatch = (gameId, form) => {
参数：
1、赛事id
2、form： // 比赛表单
        form：{
            awayTeamId: '', // 客队的teamId(必填)
            homeTeamId: '', // 主队的teamId(必填)
            responsiblePersonId: '', // 负责人的userId(必填)
            phone: '', // 联系电话
            venueId: '', // 比赛场地id
            refereeId: [], // 裁判的refereeId
            start_time: '', // 比赛开始时间
            end_time: '', // 比赛结束时间
        },
}
```



**注意：有关getXXXoptions的方法，全部移到最后的option的api里面解决**

建议后端建一个option的controller



### 页面：/my/match/{matchId}(新增接口)

1、根据赛事id，获取比赛的基本信息数据

备注：这个比赛的所属赛事，也必须是查询者所创建的，返回的属性全部都要有

```
export const getMatchData = (matchId) => {
参数：
matchId：'M20231128002' // 赛事id

返回数据：
data：{
        matchId: 'M20231128002', // 赛事id
        sport: "篮球", // 赛事项目(无需id，因为不允许修改)
        awayTeamId: '123', // 修改客队球员得分信息时使用
        homeTeamId: '456', // 修改主队球员得分信息时使用
        awayTeam: '软件1223', // 客队名称
        homeTeam: '软件1224', // 主队名称
        venueId: "4", // 场地id
        venue: "篮球场3号", // 场地名称
        start_time: "2025-01-03 14:00", // 比赛开始时间
        end_time: "2025-01-03 15:30", // 比赛结束时间
        responsiblePersonId: '4', // 负责人的userId
        responsiblePerson: '张管理员', // 负责人名称
        phone: '13800138000', // 联系电话 
        note: '请各队提前30分钟到场热身，迟到15分钟视为弃权', // 比赛备注
        phase: 1, // 赛段
        winner: "软件1224", // 胜利者名称(或"平局"、"待定")
        referee: [{refereeId: '1', name: '张教练'}, {refereeId: '2', name: '王教练'}] // 裁判id和裁判名称
}
}
```



**2、获取比赛的赛段得分信息(不需要写，直接复用/sport/match/getQuartersData即可)**

```
export const getQuartersData = (matchId) => {

}
```



**3、获取比赛的队伍得分信息(不需要写，直接复用/sport/match/getTeamStatsData即可)**

```
export const getTeamStatsData = (matchId) => {

}
```



**4、获取比赛队伍球员的得分信息(不需要写，直接复用/sport/match/getPlayerData即可)**

```
export const getPlayersData = (matchId) => {

}
```



**5、修改比赛的基本信息数据**

```
export const updateMatchData = (matchId, form) => {
参数：
1、matchId: 'M20231128002' // 比赛id
2、form： // 修改比赛基本信息表单
		form： {
            venueId: "4", // 修改后的场地id
            start_time: "2025-01-03 14:00", // 比赛结束时间
            end_time: "2025-01-03 15:30", // 比赛开始时间
            responsiblePersonId: '4', // 修改后的负责人的用户id
            phone: '13800138000', // 联系电话
            note: '请各队提前30分钟到场热身，迟到15分钟视为弃权', // 修改后的备注信息
            phase: 1, // 修改后的赛程
            winner: "软件1224", // 修改后的胜利者
            refereeId: ['1', '2'] // 修改后的裁判id
		}
		
返回数据：修改成功或失败
}
```



**6、修改比赛的赛段得分信息**

备注：没有teamId，是因为/sport/match/getQuartersData方法没有返回这个属性，如果要加入也可以，但要重写上面的getQuartersData的返回数据

```
export const updateQuartersData = (matchId, form) => {
参数：
1、matchId： 'M20231128002' // 比赛id
2、form： // 修改表单
		form: [
            {	// 固定索引0为客队修改信息
              team: "Grizzlies", // 队伍名称是没必要的，因为队伍名不能修改
              one: 25,
              two: 30,
              three: 36,
              four: 25
            },
            {	// 固定索引0为主队修改信息
              team: "Warriors",
              one: 31,
              two: 36,
              three: 27,
              four: 27
            }
      	],
      	
返回数据：修改成功或失败
}
```



**7、修改比赛的队伍得分信息数据**

备注：没有teamId，是因为/sport/match/getTeamStatsData方法没有返回这个属性，如果要加入要重写上面的getQuartersData的返回数据

```
export const updateTeamStatsData = (matchId, form) => {
参数：
1、matchId: 'M20231128002' // 比赛id
2、form：// 修改表单
		form: [
            {	// 固定索引0为客队修改信息
              name: "Grizzlies", // 队伍名称是没必要的，因为队伍名不能修改
              pts: 116,
              reb: 50,
              ast: 39,
              fgPct: 48.8,
              tpPct: 46.2
            },
            {	// 固定索引0为主队修改信息
              name: "Warriors",
              pts: 121,
              reb: 39,
              ast: 29,
              fgPct: 45.9,
              tpPct: 34.9
            }
      ]
      
返回数据：修改成功或失败
}
```



**8、修改球员得分信息数据**

备注：teamId来源于/my/match/{matchId}/getMatchData

```
export const updatePlayersData = (matchId, form) => {
参数：
1、matchId: 'M20231128002' // 比赛id
2、form：// 修改表单
			form:{	playerId: 123, // 球员的用户id
					name:"莫兰特", // 没用属性，因为名字无需修改
					teamId: '', // 队伍id
					minutes:"34:54",
					pts:22,reb:3,
					ast:3,
					fgMade:9,
					fgAtt:18}
}
```



### 页面：/add(新增接口)

**1、申请新的赛事**

```
export const addGame = (form) => {
参数：
1、form: // 新增赛事表单
			form: {
                sportId: '1', // 赛事项目id
                name: '春季联赛', // 赛事名称
                register_time: ["2025-04-30 00:00", "2025-05-08 00:00"], // 报名开始时间和结束时间。索引0为开始时间、索引1为结束时间
                start_time: "2025-05-21 00:00", // 赛事开始时间(结束时间未知，无需填写)
                responsiblePersonId: '', // 负责人的用户id
                phone: '', // 负责人联系电话
                mode: 0, // 匹配模式。0-系统按时间分配，1-系统按随机分配，2-用户自定义
                note: '' // 备注
			}
			
返回数据：操作成功或失败
}
```



页面：/join(新增接口)

**1、获取经过过滤后，我所在队伍，参加比赛的比赛数量**

备注：要在userInfo里获取到userId，然后再查出所有我所在的队伍(如果过滤器指定了队伍id，则需要对队伍在进行筛选)，然后根据队伍查出所有比赛的数量

```
export const getMatchesPage = (filter) => {
参数：
1、filter: //过滤器
		filter: {
            sportId: '', // 赛事项目id
            teamId: '', // 我所在队伍的id
            state: '',
		}
		
返回数据：
data: 300
}
```



**2、获取经过过滤后，我所在队伍，参加比赛列表**

备注：返回的数据一定要在后端进行排序，将离比赛开始时间(start_time)，离当前时间近的，首先返回。

```
export const getMatches = (page, filter) => {
参数：
1、页码: 1 // 页码从1开始，建议一次返回十条数据
2、filter: 过滤器，过滤器属性为空，表示不进行该属性过滤
		filter: {
            sportId: '', // 赛事项目id
            teamId: '', // 我所在队伍的id
            state: 0, // 比赛状态：0-未开始、1-正在举行、2-已结束
		}
		
返回数据：
matches: [
    {
        matchId: 'M20231128001',
        sport: "篮球",
        awayTeam: '软件1223',
        homeTeam: '软件1224',
        venue: "篮球场1号",
        start_time: "2025-06-03 14:00",
        end_time: "2025-06-03 15:30",
    },
    {
        matchId: 'M20231128002',
        sport: "羽毛球",
        awayTeam: '软件1223',
        homeTeam: '软件1224',
        venue: "羽毛球馆2号",
        start_time: "2025-01-03 10:00",
        end_time: "2025-07-03 11:30",
    },
]
}
```



### 页面：/follow(新增接口)

**1、获取我关注的比赛的总数目**

备注：考虑到我关注的比赛不多，因此不进行过滤

```
export const getMatchesPage = () => {

返回数据：300
}
```



**2、获取我关注的比赛列表**

备注：考虑到我关注的比赛不多，因此不进行过滤

```
export const getMatches = (page) => {
参数：
page：1 // 页码

返回数据：
data: [
    {
        matchId: 'M20231128001',
        sport: "篮球",
        awayTeam: '软件1223',
        homeTeam: '软件1224',
        venue: "篮球场1号",
        start_time: "2025-06-03 14:00",
        end_time: "2025-06-03 15:30",
    },
    {
        matchId: 'M20231128002',
        sport: "羽毛球",
        awayTeam: '软件1223',
        homeTeam: '软件1224',
        venue: "羽毛球馆2号",
        start_time: "2025-01-03 10:00",
        end_time: "2025-07-03 11:30",
    },
]
}
```



**3、获取该用户是否已开启自动删除功能、和是否已开启5分钟通知功能**

```
export const getAutoDeletedAndNotice = () => {

返回数据：
data: {
      autoDeleted: 0, // 比赛结束后自动删除功能,0-未开启、1-已开启
      notice: 0 // 比赛开始前5分钟通知,0-未开启、1-已开启
}
}
```



**4、修改自动删除功能状态**

备注：将已开启改为未开启、将未开启改为已开启。如果是改为已开启，则要删除关注比赛列表中，状态为已结束的比赛

```
export const handleAutoDeleted = () => {

返回数据：成功或失败
}
```



**5、修改赛事开始前5分钟通知功能**

备注：将已开启改为未开启、将未开启改为已开启。

```
export const handleNotice = () => {

返回数据：成功或失败
}
```



### 页面：/team(新增接口)

**1、获取我创建的队伍列表**

```
export const getTeamData = (filter) => {
参数：
1、filter:[1, 3, 45] //赛事项目的过滤器

返回数据：
teamsData: [{
        teamId: 1, // 队伍id
        name: '1223', // 队伍名称
        sportId: 1, // 队伍的赛事项目id
        sport: '篮球', // 队伍的赛事项目名称
        players: [ // 队员列表
            {userId: 1, name: '张三'},
            {userId: 2, name: '李四'},
            {userId: 3, name: '王五'},
            {userId: 4, name: '赵六'},
            {userId: 5, name: '刘七'},
    	]
},
{
        teamId: 2,
        name: '1223',
        sportId: 2,
        sport: '足球',
        players: [
            {userId: 1, name: '张三'},
            {userId: 2, name: '李四'},
            {userId: 3, name: '王五'},
            {userId: 4, name: '赵六'},
            {userId: 5, name: '刘七'},
    	]
}]

}
```



**2、创建新的队伍**

备注：队伍还需要有创建者的用户id

```
export const addTeam = (form) => {
参数：
form: // 队伍信息
		form: {
            name: '软件1223',
            sportId: 'S123456',
            players: ['1', '2', '45']
		}
		
返回数据：操作成功或失败
}
```



**3、创建者删除队伍**

```
export const deleteTeam = (teamId) => {
参数：
teamId: '123' // 队伍id

返回数据：操作成功或失败
}
```



**4、创建者修改队伍信息**

```
export const updateTeam = (form) => {
参数：
form: {
        teamId: '', // 要修改的队伍teamId
        name: '',
        sportId: '',
        players: []
}

返回数据：操作成功或失败
}
```



**5、队员获取加入的队伍列表**

备注：如果队伍中，将创建者添加为第一个队员的话，那么这个队伍应该出现在上面的队伍数据。那么这里就不要返回给前端。如果没有的话，那就没事了。

```
export const getJoinData = (filter) => {
参数：
1、filter:[1, 3, 45] //赛事项目的过滤器

返回数据：
data:[{
        teamId: 1,
        name: '1223',
        sportId: 1,
        sport: '篮球',
        createPerson: '张创建', // 创建者的名字(无需id，因为参与者无权修改)。
        players: [
          {userId: 1, name: '张三'},
          {userId: 2, name: '李四'},
          {userId: 3, name: '王五'},
          {userId: 4, name: '赵六'},
          {userId: 5, name: '刘七'},
        ]
      },
        {
          teamId: 2,
          name: '1223',
          sportId: 2,
          sport: '足球',
          createPerson: '张创建',
          players: [
            {userId: 1, name: '张三'},
            {userId: 2, name: '李四'},
            {userId: 3, name: '王五'},
            {userId: 4, name: '赵六'},
            {userId: 5, name: '刘七'},
          ]
        },
}
```



**6、退出队伍**

```
export const exitTeam = (teamId) => {
参数：
teamId: '123' //队伍id

返回数据：操作成功或失败
}
```



### 页面：/join(新增接口)

**1、获取经过过滤后，我所在队伍报名的比赛总数目**

```
export const getMatchesPage = (filter) => {
参数：
filter: { // 过滤器
        sportId: '', // 赛事项目id
        teamId: '', // 队伍id(建议：由于队伍本身就有赛事项目属性，因此如果该值存在的话，可以无视上面的sportId)
        state: '', // 比赛状态：0-未开始、1-正在举行、2-已结束
      }
      
返回数据：300
}
```



**2、获取经过过滤后，我创建或所在的队伍的报名比赛列表**

备注：如果队伍中，将创建者添加为第一个队员的话。

```
export const getMatches = (page, filter) => {
参数：
page：1 // 页码：从1开始，建议一次返回10条数据
filter: { // 过滤器
        sportId: '',
        teamId: '',
        state: '', // 比赛状态：0-未开始、1-正在举行、2-已结束
      }

返回数据：
data：[
    {
        matchId: 'M20231128001',
        sport: "篮球",
        awayTeam: '软件1223',
        homeTeam: '软件1224',
        venue: "篮球场1号",
        start_time: "2025-06-03 14:00",
        end_time: "2025-06-03 15:30",
    },
    {
        matchId: 'M20231128002',
        sport: "羽毛球",
        awayTeam: '软件1223',
        homeTeam: '软件1224',
        venue: "羽毛球馆2号",
        start_time: "2025-01-03 10:00",
        end_time: "2025-07-03 11:30",
    }
]
}
```



### 页面：/audit(新增接口)

**1、获取经过过滤后的、符合条件的赛事数量**

```
export const getCompetitionPage = (filter) => {
参数：
1、filter: {
        review_status: "0"(字符串形式) // 审核状态。0-未审核、1-已通过、2-已否决、3-已撤销
        gameId: '',
        sportId: '',
        responsiblePersonId: '', // 负责人的用户id
      },
      
返回数据：300
}
```



**2、获取经过过滤后的、符合条件的赛事列表**

```
export const getCompetitionData = (page, filter) => {
参数：
1、page：页码
2、filter：{
        review_status: "0"(字符串形式) // 审核状态。0-未审核、1-已通过、2-已否决、3-已撤销
        gameId: '', // 赛事id
        sportId: '',
        responsiblePersonId: '', // 负责人的用户id
   },
}
```



**3、通过"待审核"的赛事**

```
export const agreeGame = (gameId) => {
参数：
1、gameId：'123' // 赛事id

返回数据：成功或失败
}
```



**4、不通过"待审核"的赛事**

```
export const disagreeGame = (gameId, reason) => {
参数：
1、gameId：'123' // 赛事id
2、reason: '赛事不合规' // 不通过的原因


返回数据：成功或失败
}
```



**5、撤销“已通过”的赛事**

```
export const cancelGame = (gameId, reason) => {
1、gameId：'123' // 赛事id
2、reason: '赛事不合规' // 撤销的原因


返回数据：成功或失败
}
```



### 页面：/enroll/{gameId}(新增接口)

**1、获取我创建的队伍信息，(！！！)其中需要该队伍是否有报名该赛事的属性**

```
export const getTeamData = (sportId, gameId) => {
参数：
1、sportId: // 赛事项目id
2、gameId // 赛事id，用于检查队伍是否有报名该赛事

返回数据：
data: [{
            teamId: 1,
            name: '1223',
            sportId: 1,
            sport: '篮球',
            is_enroll: 0, // 该队伍是否报名了该赛事：0-未报名、1-已报名
            players: [
            {userId: 1, name: '张三'},
            {userId: 2, name: '李四'},
            {userId: 3, name: '王五'},
            {userId: 4, name: '赵六'},
            {userId: 5, name: '刘七'},]
      },
      {
            teamId: 2,
            name: '1223',
            sportId: 2,
            sport: '足球',
            is_enroll: 0, // 该队伍是否报名了该赛事：0-未报名、1-已报名
            players: [
            {userId: 1, name: '张三'},
            {userId: 2, name: '李四'},
            {userId: 3, name: '王五'},
            {userId: 4, name: '赵六'},
            {userId: 5, name: '刘七'},]
       },
]

}
```



**2、为队伍报名赛事**

```
export const enrollGame = (form) => {
参数：
form：// 报名表单
		form: {
            responsiblePersonId: '', // 负责人的用户id
            phone: '', // 联系电话
            teamId: '' // 报名队伍id
		}
		
返回数据：成功或失败
}
```



**3、为已报名队伍取消报名**

```
export const unrollGame = (teamId, gameId) => {
参数：
teamId: // 取消报名的队伍id
gameId: // 赛事id

返回数据：成功或失败
}
```



### 页面：/referee(新增接口)

**1、根据关键字获取裁判数据列表**

备注：裁判使用的是下拉列表，因此不需要获取到总数量

```
export const getRefereeData = (page, key) => {
参数：
1、page：页码，从1开始
2、搜索关键字，建议和裁判名字进行模糊匹配

返回数据：
data: [
        {
        refereeId: 10000, // 裁判id
        url: "https://tse2-mm.cn.bing.net/th/id/OIP-C.fRliYqG5hxDcZSQAToUSwAAAAA?rs=1&pid=ImgDetMain", // 图片url
        referee_name: '张三', // 裁判名字
        phone: '123456789', // 联系电话
        sports: ["篮球", "足球", "羽毛球"], // 擅长赛事项目
        honours: ['国家一级运动员', '国家二级教练'], // 个人荣誉
      }, {
        refereeId: 10001,
        url: "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png",
        referee_name: '李四',
        phone: '987654321',
        sports: ["篮球", "足球", "羽毛球"],
        honours: ['国家二级运动员', '国家一级教练', '月最佳员工'],
      }
  ]
}
```



**2、删除裁判**

```
export const deleteReferee = (refereeId) => {
参数：
refereeId：'123' // 裁判id
}
```



**3、新增裁判**

```
export const addReferee = (form) => {
参数：
form: {
    name: '', // 裁判姓名
    phone: '', // 联系电话
    sportIds: ['123', '456'], // 擅长的赛事运动项目
    honours: ['国家一级运动员', '月最佳员工'] // 个人荣誉
},
}
```



### 模块：options

**1、获取所有的赛事项目选项**

```
export const getSportOptions = () => {

返回数据：
data: [
    {
        sportId: '123',
    	name: '篮球'
    },
    {
        sportId: '123',
    	name: '篮球'
    }
]
}
```



**2、获取匹配关键字的场地选项**

```
export const getVenueOptions = (key) => {
参数：
key: '' // 关键字

返回数据：
data: [
    {
        venueId: '123',
    	name: '篮球馆1号'
    },
    {
        venueId: '123',
    	name: '篮球馆2号'
    }
]
}
```



**3、获取匹配关键字的负责人选项**

备注：关键字建议对userId进行精确匹配、用户名进行模糊匹配

本质上是对所有用户进行筛选查询

```
export const getResponsiblePersonOptions = (key) => {
参数：
key: '' // 关键字

返回数据：
data: [
    {
        userId: '123',
    	name: '张三'
    },
    {
        userId: '123',
    	name: '李四'
    }
]
}
```



**4、获取匹配关键字的裁判选项**

```
export const getRefereeOptions = (key) => {
参数：
key: '' // 关键字

返回数据：
data: [
    {
        refereeId: '123',
    	name: '张三'
    },
    {
        refereeId: '123',
    	name: '李四'
    }
]
}
```



**5、获取匹配关键字的球员信息**

本质上是对所有用户进行筛选查询

```
export const getPlayerOptions = (key) => {
参数：
key: '' // 关键字

返回数据：
data: [
    {
        refereeId: '123',
    	name: '张三'
    },
    {
        refereeId: '123',
    	name: '李四'
    }
]
}
```



**6、获取所有的我所在的队伍的选项**

```
export const getJoinTeamOptions = () => {
返回数据：
data: [
    {
        teamId: '123',
    	name: '软件1223'
    },
    {
        teamId: '124',
    	name: '软件1224'
    }
]
}
```



**7、获取所有的，报名了该赛事的队伍的选项**

```
export const getGameTeamOptions = (gameId) => {
参数：
gameId：赛事id

返回数据：
data: [
    {
        refereeId: '123',
    	name: '张三'
    },
    {
        refereeId: '123',
    	name: '李四'
    }
]
}
``` 