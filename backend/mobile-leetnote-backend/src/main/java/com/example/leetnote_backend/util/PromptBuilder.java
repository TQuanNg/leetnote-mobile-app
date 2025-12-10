package com.example.leetnote_backend.util;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    public String buildPrompt(String problemText, String pseudocode) {

        return String.format("""
                You are an AI code reviewer. Evaluate the user's pseudocode for the following problem.
                
                Problem:
                %s
                
                User pseudocode:
                %s
                
                Before scoring:
                - First, determine if the user's input is actually pseudocode or even relevant to the problem.
                - If the input is irrelevant (e.g., general text, requests, or statements unrelated to solving the problem),
                  immediately set rating = 1 and explain that no pseudocode was provided.
                
                Scoring rules:
                1. If the response is vague, just a single statement (e.g., "use while loop" or "sort the array"), or does not include step-by-step logic or control flow — assign rating = 1.
                2. Use 2–4 only if the pseudocode shows an attempt to outline logic, control flow, or conditions but is missing important details.
                3. Use 5 only if the pseudocode provides a clear, structured, and mostly correct algorithmic solution.
                4. Do not assume correctness from keywords alone — there must be logical flow.
                
                Feedback style:
                - If rating = 1: Be encouraging and guide the user to start by outlining concrete steps, not just ideas.
                - If rating = 2–4: Point out specific logical gaps or unclear flow.
                - If rating = 5: Briefly praise and suggest one small improvement.
                - If no major issue, set "issue" = ["No major issues found"].
                
                Return JSON only with these keys:
                {
                  "rating": (1–5),
                  "issue": [list of 1–2 short bullet points],
                  "feedback": [list of 1–2 concise suggestions]
                }
                """, problemText, pseudocode);
    }
}
