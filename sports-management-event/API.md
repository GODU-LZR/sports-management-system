好的，根据您的要求，我将这些前端 API 调用转换为后端 API 接口文档，并按照页面/模块进行组织，将 `getXXXOptions` 方法全部移到 `options` 模块，并为每个接口添加路径和 HTTP 方法建议。

建议后端确实建立一个 `option` 控制器（或服务），专门处理各种下拉列表和选项数据。

---

## API 接口文档

### 通用说明

*   所有接口请求和返回数据格式建议使用 JSON。
*   需要用户登录的接口，后端应通过 Session、Token 或其他认证机制获取当前用户 ID。
*   路径中的 `{param}` 表示路径参数。
*   `filter` 参数如果复杂，建议使用 POST 方法并将 filter 放在请求体中；如果简单，可以使用 GET 方法并作为查询参数。这里为了统一和清晰，对于带 `filter` 的查询，建议使用 GET 方法，并将 filter 属性作为查询参数传递。对于创建和修改操作，使用 POST 或 PUT/PATCH，并将表单数据放在请求体中。

---

### 模块：`sport` (公共赛事信息)

#### 页面：`/sport/type`

1.  **获取带赛事列表的赛事项目**
    *   **描述:** 获取所有赛事项目，并嵌套包含该项目下的部分赛事列表。
    *   **方法:** `GET`
    *   **路径:** `/sport/type/with-games`
    *   **参数:**
        *   无
    *   **返回数据:**
        ```json
        {
          "code": 200, // 或其他状态码
          "message": "成功", // 或其他信息
          "data": [
            {
              "sportId": 123451,
              "name": "篮球",
              "games": [
                { "gameId": 123, "name": "NBA季后赛" },
                { "gameId": 123, "name": "CBA常规赛" }
              ]
            },
            {
              "sportId": 123452,
              "name": "足球",
              "games": [
                { "gameId": 123, "name": "NBA季后赛" },
                { "gameId": 123, "name": "CBA常规赛" }
              ]
            }
          ]
        }
        ```

2.  **获取不带赛事列表的赛事项目数据**
    *   **描述:** 获取所有赛事项目，不包含具体的赛事列表。
    *   **方法:** `GET`
    *   **路径:** `/sport/type`
    *   **参数:**
        *   无
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "sportId": 123451, "name": "篮球" },
            { "sportId": 123452, "name": "足球" }
          ]
        }
        ```

#### 页面：`/sport/competition/{sportId}`

1.  **获取指定赛事项目、经过过滤后的赛事的总条数**
    *   **描述:** 根据赛事项目 ID 和过滤器条件，统计符合条件的、**已通过审核**的赛事数量。
    *   **方法:** `GET`
    *   **路径:** `/sport/competition/{sportId}/count`
    *   **参数:**
        *   `sportId`: `{sportId}` (路径参数) - 赛事项目 ID
        *   `name`: (查询参数) - 赛事名称，建议模糊匹配
        *   `state`: (查询参数) - 赛事状态 (0-不可报名, 1-可报名, 2-未开始, 3-正在举行, 4-已结束)
        *   `registerTime`: (查询参数) - 报名时间值 (yyyy-MM-dd HH:mm)，判断是否在赛事报名时间内
        *   `time`: (查询参数) - 举办时间值 (yyyy-MM-dd HH:mm)，判断是否在赛事举办时间内
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": 300 // 符合条件的赛事数量
        }
        ```
    *   **注意:** **需修改** 新增了过滤器参数，且只统计**已通过审核**的赛事。赛事状态由后端根据当前时间、报名时间、举办时间计算得出。

2.  **获取符合条件的、已通过审核的赛事列表**
    *   **描述:** 根据赛事项目 ID、页码和过滤器条件，获取符合条件的、**已通过审核**的赛事列表。
    *   **方法:** `GET`
    *   **路径:** `/sport/competition/{sportId}`
    *   **参数:**
        *   `sportId`: `{sportId}` (路径参数) - 赛事项目 ID
        *   `page`: (查询参数) - 页码 (从 1 开始)
        *   `pageSize`: (查询参数, 建议添加) - 每页数量 (默认 10)
        *   `name`: (查询参数) - 赛事名称，建议模糊匹配
        *   `state`: (查询参数) - 赛事状态
        *   `registerTime`: (查询参数) - 报名时间值
        *   `time`: (查询参数) - 举办时间值
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "gameId": 1,
              "name": "春季联赛",
              "sport": "篮球", // 赛事项目名称
              "responsiblePeople": "张三", // 负责人名称
              "phone": "12345678912",
              "register_start_time": "2026-01-01 00:00",
              "register_end_time": "2026-03-01 00:00",
              "start_time": "2026-04-01 00:00",
              "end_time": "2026-05-01 00:00"
              // "mode": 1 // 如果需要也可以返回
            },
            {
              "gameId": 2,
              "name": "秋季联赛",
              "sport": "篮球",
              "responsiblePeople": "李四",
              "phone": "12345678912",
              "register_start_time": "2023-01-01 00:00",
              "register_end_time": "2026-03-01 00:00",
              "start_time": "2026-04-01 00:00",
              "end_time": "2026-05-01 00:00"
              // "mode": 1
            }
          ]
        }
        ```
    *   **注意:** **需修改** 返回的赛事都必须是**已通过审核**的赛事。建议一次返回 10 条数据。

#### 页面：`/sport/game/{gameId}`

1.  **获取赛事的基本信息**
    *   **描述:** 根据赛事 ID 获取赛事的详细基本信息。
    *   **方法:** `GET`
    *   **路径:** `/sport/game/{gameId}`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": {
            "gameId": 1,
            "name": "春季联赛",
            "sport": "篮球", // 赛事项目名称
            "responsiblePeople": "张三", // 负责人名称
            "phone": "12345678912",
            "register_start_time": "2026-01-01 00:00",
            "register_end_time": "2026-03-01 00:00",
            "start_time": "2026-04-01 00:00",
            "end_time": "", // 赛事举办结束时间
            "note": "备注信息"
            // "mode": 1 // 如果需要也可以返回
          }
        }
        ```
    *   **注意:** **需修改** 返回的赛事都必须是**已通过审核**的赛事。

2.  **获取赛事的对应比赛列表**
    *   **描述:** 根据赛事 ID 获取该赛事下的所有比赛列表。
    *   **方法:** `GET`
    *   **路径:** `/sport/game/{gameId}/matches`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "matchId": "M20231128001",
              "sport": "篮球", // 赛事项目名称
              "awayTeam": "软件1223", // 客队名称
              "homeTeam": "软件1224", // 主队名称
              "awayTeam_score": 20, // 客队得分
              "homeTeam_score": 10, // 主队得分
              "venue_name": "篮球场1号", // 比赛场地名称
              "start_time": "2025-01-03 14:00",
              "end_time": "2025-01-03 15:30",
              "is_followed": 1, // 该用户是否关注了该比赛。0-未关注，1-已关注 (需要用户登录)
              "phase": 1, // 赛段
              "winner": "软件1224" // 获胜队伍名称
            },
            {
              "matchId": "M20231128002",
              "sport": "羽毛球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "awayTeam_score": 20,
              "homeTeam_score": 10,
              "venue_name": "羽毛球馆2号",
              "start_time": "2025-01-03 10:00",
              "end_time": "2025-01-03 11:30",
              "is_followed": 0, // 需要用户登录
              "phase": 1,
              "winner": "软件1224"
            }
          ]
        }
        ```
    *   **备注:** `is_followed` 属性需要根据当前登录用户判断。

#### 页面：`/sport/match/{matchId}`

1.  **获取比赛的基本信息数据**
    *   **描述:** 根据比赛 ID 获取比赛的详细基本信息。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": {
            "matchId": "M20231128002",
            "sport": "篮球", // 赛事项目名称
            "venue_name": "篮球馆1号", // 比赛场地名称
            "referee_name": ["王裁判", "张裁判", "李裁判"], // 裁判名称数组
            "start_time": "2024-03-15 14:00",
            "end_time": "2024-03-15 16:00",
            "responsiblePerson": "张管理员", // 负责人名称
            "phone": "13800138000",
            "note": "请各队提前30分钟到场热身，迟到15分钟视为弃权",
            "is_followed": 1 // 该用户是否关注比赛。0-未关注、1-已关注 (需要用户登录)
          }
        }
        ```
    *   **备注:** `is_followed` 属性需要根据当前登录用户判断。

2.  **获取比赛的球员得分**
    *   **描述:** 根据比赛 ID 获取比赛双方队伍的球员得分统计数据。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}/players`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            [
              // 索引 0: 客队球员得分
              { "name": "莫兰特", "minutes": "34:54", "pts": 22, "reb": 3, "ast": 3, "fgMade": 9, "fgAtt": 18 },
              // ... 更多客队球员
            ],
            [
              // 索引 1: 主队球员得分
              { "name": "库里", "minutes": "38:12", "pts": 30, "reb": 5, "ast": 7, "fgMade": 10, "fgAtt": 20 },
              // ... 更多主队球员
            ]
          ]
        }
        ```
    *   **需创建:** 这是新接口。

3.  **用户关注或取消关注比赛**
    *   **描述:** 切换当前用户对指定比赛的关注状态。
    *   **方法:** `POST` (或 `PATCH`)
    *   **路径:** `/sport/match/{matchId}/follow`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "操作成功" // 或其他成功/失败信息
        }
        ```
    *   **备注:** 需要用户登录。

4.  **获取比赛的赛段得分信息**
    *   **描述:** 根据比赛 ID 获取比赛的赛段得分信息。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}/quarters`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
    *   **返回数据:** (假设结构，用户未提供具体结构，但提到了复用)
        ```json
        {
            "code": 200,
            "message": "成功",
            "data": [
                {
                    "teamName": "客队名称",
                    "quarters": [25, 30, 36, 25], // 各赛段得分
                    "total": 116
                },
                {
                    "teamName": "主队名称",
                    "quarters": [31, 36, 27, 27],
                    "total": 121
                }
            ]
        }
        ```
    *   **备注:** 复用接口，无需新建。

5.  **获取比赛的队伍得分信息**
    *   **描述:** 根据比赛 ID 获取比赛双方队伍的统计信息（总得分、篮板、助攻等）。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}/team-stats`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
    *   **返回数据:** (假设结构，用户未提供具体结构，但提到了复用)
        ```json
        {
            "code": 200,
            "message": "成功",
            "data": [
                {
                    "teamName": "客队名称",
                    "pts": 116,
                    "reb": 50,
                    "ast": 39,
                    "fgPct": 48.8, // 投篮命中率
                    "tpPct": 46.2 // 三分命中率
                },
                {
                    "teamName": "主队名称",
                    "pts": 121,
                    "reb": 39,
                    "ast": 29,
                    "fgPct": 45.9,
                    "tpPct": 34.9
                }
            ]
        }
        ```
    *   **备注:** 复用接口，无需新建。

---

### 模块：`my` (用户相关的赛事/比赛/队伍信息)

#### 页面：`/my/competition`

1.  **获取过滤后，"我的"赛事的总数目**
    *   **描述:** 统计当前用户创建的赛事中，符合指定审核状态的数量。
    *   **方法:** `GET`
    *   **路径:** `/my/competition/count`
    *   **参数:**
        *   `review_status`: (查询参数) - 审核状态 ("0"-未审核, "1"-已通过, "2"-已否决, "3"-已撤销)。**字符串类型**。
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": 300 // 符合条件的赛事数量
        }
        ```
    *   **备注:** 需要用户登录以获取 `userId`。`review_status` 是字符串类型。

2.  **获取"我的"赛事基本信息数据**
    *   **描述:** 获取当前用户创建的赛事列表，根据审核状态过滤。
    *   **方法:** `GET`
    *   **路径:** `/my/competition`
    *   **参数:**
        *   `page`: (查询参数) - 页码 (从 1 开始)
        *   `pageSize`: (查询参数, 建议添加) - 每页数量 (默认 10)
        *   `review_status`: (查询参数) - 审核状态 ("0"-未审核, "1"-已通过, "2"-已否决, "3"-已撤销)。**字符串形式**。
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "gameId": 1,
              "name": "春季联赛",
              "sport": "篮球",
              "responsiblePeople": "张三",
              "phone": "12345678912",
              "register_start_time": "2026-01-01 00:00",
              "register_end_time": "2026-03-01 00:00",
              "start_time": "2026-04-01 00:00",
              "end_time": "2026-05-01 00:00"
            },
            {
              "gameId": 2,
              "name": "秋季联赛",
              "sport": "篮球",
              "responsiblePeople": "李四",
              "phone": "12345678912",
              "register_start_time": "2023-01-01 00:00",
              "register_end_time": "2023-03-01 00:00",
              "start_time": "2026-04-01 00:00",
              "end_time": "2026-05-01 00:00"
            }
          ]
        }
        ```
    *   **注意:** **需修改** `review_status` 值类型。需要用户登录以获取 `userId`。

#### 页面：`/my/game/{gameId}`

1.  **根据gameId，获取我创建的指定赛事的基本信息**
    *   **描述:** 获取当前用户创建的指定赛事的详细基本信息，无论审核状态如何。
    *   **方法:** `GET`
    *   **路径:** `/my/game/{gameId}`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": {
            "gameId": "G20250513",
            "name": "春季联赛",
            "sport": "篮球", // 赛事项目名称
            "responsiblePersonId": "需要负责人的用户id", // 负责人的用户 ID
            "responsiblePeople": "张三", // 负责人名称
            "phone": "12345678912",
            "register_start_time": "2026-01-01 00:00",
            "register_end_time": "2026-03-01 00:00",
            "start_time": "2026-04-01 00:00",
            "end_time": "2026-04-04 00:00",
            "note": "备注信息",
            "mode": 2, // 匹配模式
            "review_status": 0, // 0-待审核、1-已通过、2-已否决、3-已撤销
            "reason": "否决原因" // 否决或撤销原因，"已通过"和"待审核"时为空
          }
        }
        ```
    *   **注意:** **需修改** 返回值新增 `reason` 和 `review_status` 属性。需要用户登录，且查询的赛事必须是该用户创建的。

2.  **获取我创建的赛事，该赛事内的比赛列表**
    *   **描述:** 获取当前用户创建的指定赛事下的所有比赛列表。
    *   **方法:** `GET`
    *   **路径:** `/my/game/{gameId}/matches`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "matchId": 123456,
              "sport": "篮球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "venue_name": "篮球场1号",
              "start_time": "2025-01-03 14:00",
              "end_time": "2025-01-03 15:30"
            },
            {
              "matchId": 123457,
              "sport": "羽毛球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "venue_name": "羽毛球馆2号",
              "start_time": "2025-01-03 10:00",
              "end_time": "2025-01-03 11:30"
            }
          ]
        }
        ```
    *   **备注:** 需要用户登录，且查询的赛事必须是该用户创建的。

3.  **修改赛事的基本信息数据**
    *   **描述:** 修改当前用户创建的指定赛事的基本信息。
    *   **方法:** `PUT` (或 `PATCH`)
    *   **路径:** `/my/game/{gameId}`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 需要修改的赛事 ID
        *   **请求体 (JSON):**
            ```json
            {
              "name": "春季联赛", // 修改后的赛事名称
              "responsiblePersonId": "123", // 修改后的负责人 userId
              "phone": "12345678912", // 修改后的联系电话
              "register_start_time": "2026-01-01 00:00", // 修改后的赛事报名开始时间
              "register_end_time": "2026-03-01 00:00", // 修改后的赛事报名结束时间
              "start_time": "2026-04-01 00:00", // 修改后的赛事开始时间
              "end_time": "", // 修改后的赛事结束时间
              "note": "备注信息", // 修改后的备注信息
              "mode": 2 // 修改后的匹配模式
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "修改成功" // 或其他成功/失败信息
        }
        ```
    *   **备注:** 需要用户登录，且修改的赛事必须是该用户创建的。建议后端直接使用提供的表单数据覆盖数据库对应字段。

4.  **创建者为自己的赛事，添加新的比赛**
    *   **描述:** 为当前用户创建的指定赛事添加新的比赛记录。
    *   **方法:** `POST`
    *   **路径:** `/my/game/{gameId}/matches`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
        *   **请求体 (JSON):**
            ```json
            {
              "awayTeamId": "", // 客队的 teamId (必填)
              "homeTeamId": "", // 主队的 teamId (必填)
              "responsiblePersonId": "", // 负责人的 userId (必填)
              "phone": "", // 联系电话
              "venueId": "", // 比赛场地 id
              "refereeIds": [], // 裁判的 refereeId 数组 (注意：前端传 id 数组)
              "start_time": "", // 比赛开始时间
              "end_time": "" // 比赛结束时间
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "添加成功" // 或其他成功/失败信息
        }
        ```
    *   **需创建:** 这是新接口。需要用户登录，且操作的赛事必须是该用户创建的。

#### 页面：`/my/match/{matchId}` (新增页面)

1.  **根据matchId，获取比赛的基本信息数据**
    *   **描述:** 获取当前用户创建的赛事下的指定比赛的详细基本信息。
    *   **方法:** `GET`
    *   **路径:** `/my/match/{matchId}`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": {
            "matchId": "M20231128002",
            "sport": "篮球", // 赛事项目名称 (不允许修改)
            "awayTeamId": "123", // 客队的 teamId
            "homeTeamId": "456", // 主队的 teamId
            "awayTeam": "软件1223", // 客队名称
            "homeTeam": "软件1224", // 主队名称
            "venueId": "4", // 场地 id
            "venue": "篮球场3号", // 场地名称
            "start_time": "2025-01-03 14:00",
            "end_time": "2025-01-03 15:30",
            "responsiblePersonId": "4", // 负责人的 userId
            "responsiblePerson": "张管理员", // 负责人名称
            "phone": "13800138000", // 联系电话
            "note": "请各队提前30分钟到场热身，迟到15分钟视为弃权", // 比赛备注
            "phase": 1, // 赛段
            "winner": "软件1224", // 胜利者名称
            "referees": [
              { "refereeId": "1", "name": "张教练" },
              { "refereeId": "2", "name": "王教练" }
            ] // 裁判 id 和名称数组
          }
        }
        ```
    *   **新增接口。** 需要用户登录，且比赛所属的赛事必须是该用户创建的。

2.  **获取比赛的赛段得分信息**
    *   **描述:** 复用公共接口，获取比赛赛段得分。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}/quarters`
    *   **参数:** `matchId` (路径参数)
    *   **备注:** 复用 `/sport/match/{matchId}/quarters` 接口。

3.  **获取比赛的队伍得分信息**
    *   **描述:** 复用公共接口，获取比赛队伍统计信息。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}/team-stats`
    *   **参数:** `matchId` (路径参数)
    *   **备注:** 复用 `/sport/match/{matchId}/team-stats` 接口.

4.  **获取比赛队伍球员的得分信息**
    *   **描述:** 复用公共接口，获取比赛球员统计信息。
    *   **方法:** `GET`
    *   **路径:** `/sport/match/{matchId}/players`
    *   **参数:** `matchId` (路径参数)
    *   **备注:** 复用 `/sport/match/{matchId}/players` 接口。

5.  **修改比赛的基本信息数据**
    *   **描述:** 修改当前用户创建的赛事下的指定比赛的基本信息。
    *   **方法:** `PUT` (或 `PATCH`)
    *   **路径:** `/my/match/{matchId}`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
        *   **请求体 (JSON):**
            ```json
            {
              "venueId": "4", // 修改后的场地 id
              "start_time": "2025-01-03 14:00", // 比赛开始时间
              "end_time": "2025-01-03 15:30", // 比赛结束时间
              "responsiblePersonId": "4", // 修改后的负责人的用户 id
              "phone": "13800138000", // 联系电话
              "note": "请各队提前30分钟到场热身，迟到15分钟视为弃权", // 修改后的备注信息
              "phase": 1, // 修改后的赛段
              "winner": "软件1224", // 修改后的胜利者 (队伍名称或特定值)
              "refereeIds": ["1", "2"] // 修改后的裁判 id 数组
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "修改成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作的比赛所属的赛事必须是该用户创建的。

6.  **修改比赛的赛段得分信息**
    *   **描述:** 修改指定比赛的赛段得分信息。
    *   **方法:** `PUT` (或 `PATCH`)
    *   **路径:** `/my/match/{matchId}/quarters`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
        *   **请求体 (JSON):**
            ```json
            [
              {
                // 索引 0: 客队修改信息
                // "team": "Grizzlies", // 队伍名称不需要传，后端应根据 matchId 确定队伍
                "one": 25,
                "two": 30,
                "three": 36,
                "four": 25
              },
              {
                // 索引 1: 主队修改信息
                // "team": "Warriors", // 队伍名称不需要传
                "one": 31,
                "two": 36,
                "three": 27,
                "four": 27
              }
            ]
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "修改成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作的比赛所属的赛事必须是该用户创建的。后端需要根据比赛 ID 确定客队和主队，并根据数组索引更新对应的赛段得分。

7.  **修改比赛的队伍得分信息数据**
    *   **描述:** 修改指定比赛的队伍统计信息。
    *   **方法:** `PUT` (或 `PATCH`)
    *   **路径:** `/my/match/{matchId}/team-stats`
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
        *   **请求体 (JSON):**
            ```json
            [
              {
                // 索引 0: 客队修改信息
                // "name": "Grizzlies", // 队伍名称不需要传
                "pts": 116,
                "reb": 50,
                "ast": 39,
                "fgPct": 48.8,
                "tpPct": 46.2
              },
              {
                // 索引 1: 主队修改信息
                // "name": "Warriors", // 队伍名称不需要传
                "pts": 121,
                "reb": 39,
                "ast": 29,
                "fgPct": 45.9,
                "tpPct": 34.9
              }
            ]
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "修改成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作的比赛所属的赛事必须是该用户创建的。后端需要根据比赛 ID 确定客队和主队，并根据数组索引更新对应的队伍统计数据。

8.  **修改球员得分信息数据**
    *   **描述:** 修改指定比赛中某个球员的统计数据。
    *   **方法:** `PUT` (或 `PATCH`)
    *   **路径:** `/my/match/{matchId}/players/{playerId}` (建议将 playerId 放在路径中)
    *   **参数:**
        *   `matchId`: `{matchId}` (路径参数) - 比赛 ID
        *   `playerId`: `{playerId}` (路径参数) - 球员的用户 ID
        *   **请求体 (JSON):**
            ```json
            {
              // "name": "莫兰特", // 名字不需要修改，无需传递或忽略
              "teamId": "", // 队伍 id (必填，用于确定是哪支队伍的球员)
              "minutes": "34:54",
              "pts": 22,
              "reb": 3,
              "ast": 3,
              "fgMade": 9,
              "fgAtt": 18
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "修改成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作的比赛所属的赛事必须是该用户创建的。

#### 页面：`/my/participating-matches` (原 `/join` 第一个部分)

1.  **获取经过过滤后，我所在队伍，参加比赛的比赛数量**
    *   **描述:** 统计当前用户所属队伍参与的比赛数量，根据过滤器条件。
    *   **方法:** `GET`
    *   **路径:** `/my/participating-matches/count`
    *   **参数:**
        *   `sportId`: (查询参数) - 赛事项目 ID
        *   `teamId`: (查询参数) - 用户所属队伍的 ID (如果存在，可忽略 sportId 过滤)
        *   `state`: (查询参数) - 比赛状态 (0-未开始, 1-正在举行, 2-已结束)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": 300 // 符合条件的比赛数量
        }
        ```
    *   **新增接口。** 需要用户登录以获取 `userId`，并查找用户所属的所有队伍。

2.  **获取经过过滤后，我所在队伍，参加比赛列表**
    *   **描述:** 获取当前用户所属队伍参与的比赛列表，根据过滤器条件，并按时间排序。
    *   **方法:** `GET`
    *   **路径:** `/my/participating-matches`
    *   **参数:**
        *   `page`: (查询参数) - 页码 (从 1 开始)
        *   `pageSize`: (查询参数, 建议添加) - 每页数量 (默认 10)
        *   `sportId`: (查询参数) - 赛事项目 ID
        *   `teamId`: (查询参数) - 用户所属队伍的 ID
        *   `state`: (查询参数) - 比赛状态 (0-未开始, 1-正在举行, 2-已结束)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "matchId": "M20231128001",
              "sport": "篮球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "venue": "篮球场1号",
              "start_time": "2025-06-03 14:00",
              "end_time": "2025-06-03 15:30"
            },
            {
              "matchId": "M20231128002",
              "sport": "羽毛球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "venue": "羽毛球馆2号",
              "start_time": "2025-01-03 10:00",
              "end_time": "2025-07-03 11:30"
            }
          ]
        }
        ```
    *   **新增接口。** 需要用户登录。后端需要根据用户 ID 找到其所属的所有队伍，然后查询这些队伍参与的比赛。返回数据应按 `start_time` 排序，离当前时间近的优先。

#### 页面：`/my/followed-matches` (原 `/follow`)

1.  **获取我关注的比赛的总数目**
    *   **描述:** 统计当前用户关注的比赛总数量。
    *   **方法:** `GET`
    *   **路径:** `/my/followed-matches/count`
    *   **参数:**
        *   无 (不进行过滤)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": 300 // 关注比赛数量
        }
        ```
    *   **新增接口。** 需要用户登录。

2.  **获取我关注的比赛列表**
    *   **描述:** 获取当前用户关注的比赛列表。
    *   **方法:** `GET`
    *   **路径:** `/my/followed-matches`
    *   **参数:**
        *   `page`: (查询参数) - 页码 (从 1 开始)
        *   `pageSize`: (查询参数, 建议添加) - 每页数量 (默认 10)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "matchId": "M20231128001",
              "sport": "篮球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "venue": "篮球场1号",
              "start_time": "2025-06-03 14:00",
              "end_time": "2025-06-03 15:30"
            },
            {
              "matchId": "M20231128002",
              "sport": "羽毛球",
              "awayTeam": "软件1223",
              "homeTeam": "软件1224",
              "venue": "羽毛球馆2号",
              "start_time": "2025-01-03 10:00",
              "end_time": "2025-07-03 11:30"
            }
          ]
        }
        ```
    *   **新增接口。** 需要用户登录。

3.  **获取该用户是否已开启自动删除功能、和是否已开启5分钟通知功能**
    *   **描述:** 获取当前用户的关注设置。
    *   **方法:** `GET`
    *   **路径:** `/my/follow-settings`
    *   **参数:**
        *   无
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": {
            "autoDeleted": 0, // 比赛结束后自动删除功能,0-未开启、1-已开启
            "notice": 0 // 比赛开始前5分钟通知,0-未开启、1-已开启
          }
        }
        ```
    *   **新增接口。** 需要用户登录。

4.  **修改自动删除功能状态**
    *   **描述:** 切换当前用户关注的比赛结束后自动删除功能的状态。
    *   **方法:** `PATCH` (或 `PUT`)
    *   **路径:** `/my/follow-settings/auto-delete`
    *   **参数:**
        *   无 (状态切换由后端根据当前状态执行)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "操作成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录。如果开启，后端应删除该用户关注列表中所有已结束的比赛。

5.  **修改赛事开始前5分钟通知功能**
    *   **描述:** 切换当前用户关注的比赛开始前 5 分钟通知功能的状态。
    *   **方法:** `PATCH` (或 `PUT`)
    *   **路径:** `/my/follow-settings/notice`
    *   **参数:**
        *   无 (状态切换由后端根据当前状态执行)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "操作成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录。

#### 页面：`/my/teams` (原 `/team` 中关于我创建的队伍部分)

1.  **获取我创建的队伍列表**
    *   **描述:** 获取当前用户创建的队伍列表，可按赛事项目过滤。
    *   **方法:** `GET`
    *   **路径:** `/my/teams`
    *   **参数:**
        *   `sportIds`: (查询参数) - 赛事项目 ID 数组 (e.g., `?sportIds=1&sportIds=3&sportIds=45`)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "teamId": 1,
              "name": "1223",
              "sportId": 1,
              "sport": "篮球", // 赛事项目名称
              "players": [
                // 队员列表
                { "userId": 1, "name": "张三" },
                { "userId": 2, "name": "李四" },
                { "userId": 3, "name": "王五" },
                { "userId": 4, "name": "赵六" },
                { "userId": 5, "name": "刘七" }
              ]
            },
            {
              "teamId": 2,
              "name": "1223",
              "sportId": 2,
              "sport": "足球",
              "players": [
                { "userId": 1, "name": "张三" },
                { "userId": 2, "name": "李四" },
                { "userId": 3, "name": "王五" },
                { "userId": 4, "name": "赵六" },
                { "userId": 5, "name": "刘七" }
              ]
            }
          ]
        }
        ```
    *   **新增接口。** 需要用户登录以获取 `userId`。

2.  **创建新的队伍**
    *   **描述:** 创建一个新的队伍，当前用户将成为创建者。
    *   **方法:** `POST`
    *   **路径:** `/team` (建议路径，创建资源)
    *   **参数:**
        *   **请求体 (JSON):**
            ```json
            {
              "name": "软件1223",
              "sportId": "S123456",
              "playerIds": ["1", "2", "45"] // 队员的用户 ID 数组 (注意：前端传 id 数组)
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "创建成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录以获取创建者 ID。

3.  **创建者删除队伍**
    *   **描述:** 删除当前用户创建的指定队伍。
    *   **方法:** `DELETE`
    *   **路径:** `/my/teams/{teamId}`
    *   **参数:**
        *   `teamId`: `{teamId}` (路径参数) - 队伍 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "删除成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作的队伍必须是该用户创建的。

4.  **创建者修改队伍信息**
    *   **描述:** 修改当前用户创建的指定队伍的信息。
    *   **方法:** `PUT` (或 `PATCH`)
    *   **路径:** `/my/teams/{teamId}`
    *   **参数:**
        *   `teamId`: `{teamId}` (路径参数) - 要修改的队伍 ID
        *   **请求体 (JSON):**
            ```json
            {
              "name": "", // 修改后的队伍名称
              "sportId": "", // 修改后的赛事项目 ID
              "playerIds": [] // 修改后的队员用户 ID 数组 (注意：前端传 id 数组)
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "修改成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作的队伍必须是该用户创建的。

#### 页面：`/my/joined-teams` (原 `/team` 中关于我加入的队伍部分)

1.  **队员获取加入的队伍列表**
    *   **描述:** 获取当前用户加入的队伍列表（不包含用户自己创建的队伍，如果创建者默认是第一个队员，则需要后端判断并排除）。
    *   **方法:** `GET`
    *   **路径:** `/my/joined-teams`
    *   **参数:**
        *   `sportIds`: (查询参数) - 赛事项目 ID 数组
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "teamId": 1,
              "name": "1223",
              "sportId": 1,
              "sport": "篮球",
              "createPerson": "张创建", // 创建者的名字
              "players": [
                // 队员列表
                { "userId": 1, "name": "张三" },
                { "userId": 2, "name": "李四" },
                { "userId": 3, "name": "王五" },
                { "userId": 4, "name": "赵六" },
                { "userId": 5, "name": "刘七" }
              ]
            },
            {
              "teamId": 2,
              "name": "1223",
              "sportId": 2,
              "sport": "足球",
              "createPerson": "张创建",
              "players": [
                { "userId": 1, "name": "张三" },
                { "userId": 2, "name": "李四" },
                { "userId": 3, "name": "王五" },
                { "userId": 4, "name": "赵六" },
                { "userId": 5, "name": "刘七" }
              ]
            }
          ]
        }
        ```
    *   **新增接口。** 需要用户登录以获取 `userId`。返回数据应排除用户自己创建的队伍（如果创建者也是队员）。

2.  **退出队伍**
    *   **描述:** 当前用户退出指定的队伍。
    *   **方法:** `DELETE`
    *   **路径:** `/my/joined-teams/{teamId}`
    *   **参数:**
        *   `teamId`: `{teamId}` (路径参数) - 队伍 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "退出成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且用户必须是该队伍的队员（但不是创建者，如果创建者不能退出）。

#### 页面：`/my/team-enrollments` (原 `/join` 第二个部分 - 已合并概念)

*Note: The second `/join` section provided by the user seems to duplicate the first one regarding listing matches. Assuming the intent was to list matches related to *my teams' enrollments*, and the first `/join` section (`/my/participating-matches`) already covers listing matches my teams are in, I will not create separate endpoints here to avoid redundancy based on the provided API names and parameters. If the user meant something different, clarification is needed.*

#### 页面：`/game/{gameId}/enrollments` (原 `/enroll/{gameId}`)

1.  **获取我创建的队伍信息，其中需要该队伍是否有报名该赛事的属性**
    *   **描述:** 获取当前用户创建的队伍列表，并指示每个队伍是否已报名指定的赛事。
    *   **方法:** `GET`
    *   **路径:** `/game/{gameId}/my-teams`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
        *   `sportId`: (查询参数) - 赛事项目 ID (可用于过滤，或后端根据 gameId 获取 sportId)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "teamId": 1,
              "name": "1223",
              "sportId": 1,
              "sport": "篮球",
              "is_enroll": 0, // 该队伍是否报名了该赛事：0-未报名、1-已报名
              "players": [
                { "userId": 1, "name": "张三" },
                { "userId": 2, "name": "李四" },
                { "userId": 3, "name": "王五" },
                { "userId": 4, "name": "赵六" },
                { "userId": 5, "name": "刘七" }
              ]
            },
            {
              "teamId": 2,
              "name": "1223",
              "sportId": 2,
              "sport": "足球",
              "is_enroll": 0, // 该队伍是否报名了该赛事：0-未报名、1-已报名
              "players": [
                { "userId": 1, "name": "张三" },
                { "userId": 2, "name": "李四" },
                { "userId": 3, "name": "王五" },
                { "userId": 4, "name": "赵六" },
                { "userId": 5, "name": "刘七" }
              ]
            }
          ]
        }
        ```
    *   **新增接口。** 需要用户登录以获取 `userId`，并查询用户创建的队伍。

2.  **为队伍报名赛事**
    *   **描述:** 为指定的队伍报名指定的赛事。
    *   **方法:** `POST`
    *   **路径:** `/game/{gameId}/enrollments`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
        *   **请求体 (JSON):**
            ```json
            {
              "responsiblePersonId": "", // 报名联系人的用户 ID
              "phone": "", // 报名联系电话
              "teamId": "" // 报名队伍 ID
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "报名成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作用户应有权限为该队伍报名（例如是队伍创建者）。

3.  **为已报名队伍取消报名**
    *   **描述:** 取消指定队伍在指定赛事的报名。
    *   **方法:** `DELETE`
    *   **路径:** `/game/{gameId}/enrollments/{teamId}`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
        *   `teamId`: `{teamId}` (路径参数) - 取消报名的队伍 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "取消报名成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要用户登录，且操作用户应有权限为该队伍取消报名。

#### 页面：`/referee` (新增页面)

1.  **根据关键字获取裁判数据列表**
    *   **描述:** 获取裁判列表，可根据关键字模糊匹配裁判姓名进行搜索，并支持分页。
    *   **方法:** `GET`
    *   **路径:** `/referee`
    *   **参数:**
        *   `page`: (查询参数) - 页码 (从 1 开始)
        *   `pageSize`: (查询参数, 建议添加) - 每页数量 (默认 10)
        *   `key`: (查询参数) - 搜索关键字 (建议模糊匹配裁判名字)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "refereeId": 10000,
              "url": "...", // 图片 url
              "referee_name": "张三",
              "phone": "123456789",
              "sports": ["篮球", "足球", "羽毛球"], // 擅长赛事项目名称
              "honours": ["国家一级运动员", "国家二级教练"] // 个人荣誉
            },
            {
              "refereeId": 10001,
              "url": "...",
              "referee_name": "李四",
              "phone": "987654321",
              "sports": ["篮球", "足球", "羽毛球"],
              "honours": ["国家二级运动员", "国家一级教练", "月最佳员工"]
            }
          ]
        }
        ```
    *   **新增接口。** 可能需要管理员权限。虽然前端说不需要总数，但分页接口通常需要返回总数以便前端计算页码，或者前端只展示“加载更多”。这里按分页接口通常做法，但备注前端需求。

2.  **删除裁判**
    *   **描述:** 删除指定的裁判记录。
    *   **方法:** `DELETE`
    *   **路径:** `/referee/{refereeId}`
    *   **参数:**
        *   `refereeId`: `{refereeId}` (路径参数) - 裁判 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "删除成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要管理员权限。

3.  **新增裁判**
    *   **描述:** 添加新的裁判记录。
    *   **方法:** `POST`
    *   **路径:** `/referee`
    *   **参数:**
        *   **请求体 (JSON):**
            ```json
            {
              "name": "", // 裁判姓名
              "phone": "", // 联系电话
              "sportIds": ["123", "456"], // 擅长的赛事运动项目 ID 数组
              "honours": ["国家一级运动员", "月最佳员工"] // 个人荣誉数组
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "添加成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要管理员权限。

#### 页面：`/audit` (新增页面)

1.  **获取经过过滤后的、符合条件的赛事数量 (待审核)**
    *   **描述:** 统计符合条件的、待审核的赛事数量。
    *   **方法:** `GET`
    *   **路径:** `/audit/games/count`
    *   **参数:**
        *   `review_status`: (查询参数) - 审核状态 ("0"-未审核, "1"-已通过, "2"-已否决, "3"-已撤销)。**字符串形式**。
        *   `gameId`: (查询参数) - 赛事 ID
        *   `sportId`: (查询参数) - 赛事项目 ID
        *   `responsiblePersonId`: (查询参数) - 负责人的用户 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": 300 // 符合条件的赛事数量
        }
        ```
    *   **新增接口。** 需要管理员或审核员权限。

2.  **获取经过过滤后的、符合条件的赛事列表 (待审核)**
    *   **描述:** 获取符合条件的、待审核的赛事列表，并支持分页。
    *   **方法:** `GET`
    *   **路径:** `/audit/games`
    *   **参数:**
        *   `page`: (查询参数) - 页码
        *   `pageSize`: (查询参数, 建议添加) - 每页数量
        *   `review_status`: (查询参数) - 审核状态 ("0"-未审核, "1"-已通过, "2"-已否决, "3"-已撤销)。**字符串形式**。
        *   `gameId`: (查询参数) - 赛事 ID
        *   `sportId`: (查询参数) - 赛事项目 ID
        *   `responsiblePersonId`: (查询参数) - 负责人的用户 ID
    *   **返回数据:** (结构应包含 `/my/game/{gameId}` 中返回的所有属性，包括 `review_status` 和 `reason`)
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            {
              "gameId": "G20250513",
              "name": "春季联赛",
              "sport": "篮球",
              "responsiblePersonId": "...",
              "responsiblePeople": "张三",
              "phone": "...",
              "register_start_time": "...",
              "register_end_time": "...",
              "start_time": "...",
              "end_time": "...",
              "note": "...",
              "mode": 0,
              "review_status": 0, // 待审核
              "reason": "" // 待审核时 reason 为空
            }
            // ... 更多赛事
          ]
        }
        ```
    *   **新增接口。** 需要管理员或审核员权限。

3.  **通过"待审核"的赛事**
    *   **描述:** 将指定 ID 的赛事审核状态更新为“已通过”。
    *   **方法:** `PATCH` (或 `PUT`)
    *   **路径:** `/audit/games/{gameId}/approve`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "操作成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要管理员或审核员权限。

4.  **不通过"待审核"的赛事**
    *   **描述:** 将指定 ID 的赛事审核状态更新为“已否决”，并记录原因。
    *   **方法:** `PATCH` (或 `PUT`)
    *   **路径:** `/audit/games/{gameId}/reject`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
        *   **请求体 (JSON):**
            ```json
            {
              "reason": "赛事不合规" // 不通过的原因
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "操作成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要管理员或审核员权限。

5.  **撤销“已通过”的赛事**
    *   **描述:** 将指定 ID 的赛事审核状态更新为“已撤销”，并记录原因。
    *   **方法:** `PATCH` (或 `PUT`)
    *   **路径:** `/audit/games/{gameId}/cancel`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
        *   **请求体 (JSON):**
            ```json
            {
              "reason": "赛事不合规" // 撤销的原因
            }
            ```
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "操作成功" // 或其他成功/失败信息
        }
        ```
    *   **新增接口。** 需要管理员或审核员权限。

---

### 模块：`options` (各种下拉列表/选项数据)

**建议后端建立一个 `option` 控制器或服务来处理这些接口。**

#### 路径前缀：`/options`

1.  **获取所有的赛事项目选项**
    *   **描述:** 获取所有赛事项目的 ID 和名称列表，用于下拉选择。
    *   **方法:** `GET`
    *   **路径:** `/options/sports`
    *   **参数:**
        *   无
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "sportId": "123", "name": "篮球" },
            { "sportId": "456", "name": "足球" }
            // ... 更多赛事项目
          ]
        }
        ```

2.  **获取匹配关键字的场地选项**
    *   **描述:** 根据关键字模糊匹配场地名称，获取场地 ID 和名称列表，用于下拉选择。
    *   **方法:** `GET`
    *   **路径:** `/options/venues`
    *   **参数:**
        *   `key`: (查询参数) - 搜索关键字
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "venueId": "123", "name": "篮球馆1号" },
            { "venueId": "456", "name": "篮球馆2号" }
            // ... 更多场地
          ]
        }
        ```

3.  **获取匹配关键字的负责人选项**
    *   **描述:** 根据关键字模糊匹配用户名称（或精确匹配用户 ID），获取用户 ID 和名称列表，用于选择负责人。
    *   **方法:** `GET`
    *   **路径:** `/options/users/responsible-person`
    *   **参数:**
        *   `key`: (查询参数) - 搜索关键字 (用户 ID 或用户名)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "userId": "123", "name": "张三" },
            { "userId": "456", "name": "李四" }
            // ... 更多用户
          ]
        }
        ```
    *   **备注:** 本质上是对用户表进行筛选查询。

4.  **获取匹配关键字的裁判选项**
    *   **描述:** 根据关键字模糊匹配裁判名称，获取裁判 ID 和名称列表，用于选择裁判。
    *   **方法:** `GET`
    *   **路径:** `/options/referees`
    *   **参数:**
        *   `key`: (查询参数) - 搜索关键字 (裁判名称)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "refereeId": "123", "name": "张三" },
            { "refereeId": "456", "name": "李四" }
            // ... 更多裁判
          ]
        }
        ```

5.  **获取匹配关键字的球员信息**
    *   **描述:** 根据关键字模糊匹配用户名称，获取用户 ID 和名称列表，用于选择球员。
    *   **方法:** `GET`
    *   **路径:** `/options/users/player`
    *   **参数:**
        *   `key`: (查询参数) - 搜索关键字 (用户名称)
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "userId": "123", "name": "张三" },
            { "userId": "456", "name": "李四" }
            // ... 更多用户
          ]
        }
        ```
    *   **备注:** 本质上是对用户表进行筛选查询。前端函数名写的是 `refereeId`，返回值写的是 `refereeId` 和 `name`，但描述是“球员信息”，且本质是对用户进行筛选。这里按描述和意图，返回 `userId` 和 `name` 更合理。

6.  **获取所有的我所在的队伍的选项**
    *   **描述:** 获取当前用户所属的所有队伍的 ID 和名称列表，用于下拉选择。
    *   **方法:** `GET`
    *   **路径:** `/options/my-teams` (包含创建和加入的) 或 `/options/my-joined-teams` (仅加入的)
    *   **参数:**
        *   无
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "teamId": "123", "name": "软件1223" },
            { "teamId": "124", "name": "软件1224" }
            // ... 更多队伍
          ]
        }
        ```
    *   **备注:** 需要用户登录。路径名建议取决于是否包含用户创建的队伍。如果需要区分，可以有两个接口。如果前端只需要用户能选择的队伍（创建或加入），`/options/my-teams` 即可。

7.  **获取所有的，报名了该赛事的队伍的选项**
    *   **描述:** 获取已报名指定赛事的队伍的 ID 和名称列表，用于下拉选择（例如在创建比赛时选择参赛队伍）。
    *   **方法:** `GET`
    *   **路径:** `/options/game/{gameId}/enrolled-teams`
    *   **参数:**
        *   `gameId`: `{gameId}` (路径参数) - 赛事 ID
    *   **返回数据:**
        ```json
        {
          "code": 200,
          "message": "成功",
          "data": [
            { "teamId": "123", "name": "队伍A" },
            { "teamId": "456", "name": "队伍B" }
            // ... 更多已报名队伍
          ]
        }
        ```
    *   **备注:** 前端函数名写的是 `getGameTeamOptions`，返回数据结构示例写的是 `refereeId` 和 `name`。这显然是复制错误。根据描述，应该返回 `teamId` 和 `name`。这里已更正。

---

这份文档根据您的描述和要求进行了整理和补充，希望对您有所帮助。