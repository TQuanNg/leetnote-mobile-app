package com.example.leetnote_backend.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GraphQLResponse {
    private DataNode data;

    @Data
    public static class DataNode {
        private MatchedUser matchedUser;

        @Data
        public static class MatchedUser {
            private SubmitStats submitStats;
        }

        @Data
        public static class SubmitStats {
            private AcSubmissionNum[] acSubmissionNum;
        }

        @Data
        public static class AcSubmissionNum {
            private String difficulty;
            private int count;
            private int submissions;
        }
    }
}
