package com.example.personalgrowthcompass.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://localhost:3000")  // Allow requests from React app
public class QuizController {

    // Counters for Learning Styles
    private double[] learningStyleCounters = new double[4]; // 0: Visual, 1: Auditory, 2: Reading/Writing, 3: Kinesthetic
    private int learningStyleQuestionCount = 1;  // For Learning Style Quiz (1-5)
    private int wellbeingQuestionCount = 6;      // For Well-being Quiz (6-9)
    private int[] wellbeingScores = new int[4];  // For storing well-being scores

    // Submit answer for Learning Style quiz (Questions 1-5)
    @PostMapping("/answer/learning-style")
    public void submitLearningStyleAnswer(@RequestBody Map<String, Integer> request) {
        int option = request.get("option");

        if (learningStyleQuestionCount < 1 || learningStyleQuestionCount > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learning style question count out of range (1-5).");
        }

        if (option < 1 || option > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learning style option must be between 1 and 4.");
        }

        learningStyleCounters[option - 1]++;
        learningStyleQuestionCount++;
    }

    // Submit answer for Well-being quiz (Questions 6-9)
    @PostMapping("/answer/wellbeing")
    public void submitWellbeingAnswer(@RequestBody Map<String, Integer> request) {
        int option = request.get("option");

        if (wellbeingQuestionCount < 6 || wellbeingQuestionCount > 9) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Well-being question count out of range (6-9).");
        }

        if (option < 1 || option > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Well-being option must be between 1 and 4.");
        }

        wellbeingScores[wellbeingQuestionCount - 6] += option;  // Store score based on question index
        wellbeingQuestionCount++;
    }

    // Output learning style and well-being results
    @GetMapping("/results/summary")
    public Map<String, Object> getQuizSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Learning Style Results
        double[] learningStyleResults = getLearningStyleResults();
        summary.put("learningStyle", learningStyleResults);

        // Well-being Results
        Map<String, Object> wellbeingResults = getWellbeingResults();
        summary.put("wellbeingResults", wellbeingResults);

        return summary;
    }

    // Output results for Learning Style
    private double[] getLearningStyleResults() {
        double[] outcome = new double[5];
        outcome[0] = determineLearningStyle();  // Dominant learning style
        double[] breakdown = percentageLearningStyle();  // Percentage breakdown

        System.arraycopy(breakdown, 0, outcome, 1, breakdown.length);

        resetLearningStyleCounters();
        return outcome;
    }

    // Output results for Well-being along with recommendations
    private Map<String, Object> getWellbeingResults() {
        Map<String, Object> results = new HashMap<>();

        results.put("Stress Management", analyseCategory(0));
        results.put("Conflict Handling", analyseCategory(1));
        results.put("Stress Levels", analyseCategory(2));
        results.put("Work Satisfaction", analyseCategory(3));

        resetWellbeingScores();
        return results;
    }

    // Analyze well-being category and provide recommendation
    private Map<String, Object> analyseCategory(int categoryIndex) {
        Map<String, Object> analysis = new HashMap<>();
        int score = wellbeingScores[categoryIndex];
        analysis.put("Score", score);
        analysis.put("Recommendation", provideWellbeingRecommendation(categoryIndex, score));
        return analysis;
    }

    // Provide recommendations based on well-being scores
    private String provideWellbeingRecommendation(int categoryIndex, int score) {
        String categoryName = getCategoryName(categoryIndex);
        String recommendation;

        if (score >= 3) {
            recommendation = "You're doing great in " + categoryName + "! Keep up the good work.";
        } else if (score >= 2) {
            recommendation = "You're doing well in " + categoryName + ", but there's room for improvement.";
        } else {
            recommendation = "You may want to focus on improving your " + categoryName + ". Consider seeking resources for help.";
        }

        return recommendation;
    }

    // Helper function to map category index to names
    private String getCategoryName(int categoryIndex) {
        switch (categoryIndex) {
            case 0: return "Stress Management";
            case 1: return "Work Problems";
            case 2: return "Stress Levels";
            case 3: return "Work Satisfaction";
            default: return "Unknown Category";
        }
    }

    // Determine dominant learning style
    private int determineLearningStyle() {
        int maxIndex = 0;
        for (int i = 1; i < learningStyleCounters.length; i++) {
            if (learningStyleCounters[i] > learningStyleCounters[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex + 1; // Returning as 1-indexed
    }

    // Percentage breakdown of learning styles
    private double[] percentageLearningStyle() {
        double[] breakdown = new double[4];
        double total = 0;

        for (double counter : learningStyleCounters) {
            total += counter;
        }

        if (total == 0) {
            return new double[]{25, 25, 25, 25}; // Evenly mixed
        }

        for (int i = 0; i < learningStyleCounters.length; i++) {
            breakdown[i] = (learningStyleCounters[i] / total) * 100;
        }
        return breakdown;
    }

    // Reset learning style counters after completion
    private void resetLearningStyleCounters() {
        for (int i = 0; i < learningStyleCounters.length; i++) {
            learningStyleCounters[i] = 0;
        }
        learningStyleQuestionCount = 1; // Reset for a new session
    }

    // Reset well-being scores after completion
    private void resetWellbeingScores() {
        for (int i = 0; i < wellbeingScores.length; i++) {
            wellbeingScores[i] = 0;
        }
        wellbeingQuestionCount = 6; // Reset question count for new session
    }

    // Recommendation based on learning style
    @GetMapping("/recommendation")
    public Map<String, String> getRecommendation(@RequestParam("interestArea") String interestArea,
                                                 @RequestParam("learningStyle") String learningStyle) {
        return recommendWorkshop(interestArea, learningStyle);
    }

    public Map<String, String> recommendWorkshop(String interestArea, String learningStyle) {
        Map<String, String> recommendations = new HashMap<>();

        switch (interestArea) {
            case "Admin":
                recommendations.put("Visual", "Digital Tools for Administrative Efficiency");
                recommendations.put("Auditory", "Effective Communication for Administrative Roles");
                recommendations.put("Reading/Writing", "Documentation Best Practices");
                recommendations.put("Kinesthetic", "Practical Time Management Techniques");
                break;
            case "Commercial":
                recommendations.put("Visual", "Data Visualization for Business Decisions");
                recommendations.put("Auditory", "Sales Pitch Masterclass");
                recommendations.put("Reading/Writing", "Market Analysis Reports");
                recommendations.put("Kinesthetic", "Negotiation Skills Workshop");
                break;
            case "Corporate Communications":
                recommendations.put("Visual", "Visual Storytelling for Corporate Branding");
                recommendations.put("Auditory", "Crisis Communication: Live Simulation");
                recommendations.put("Reading/Writing", "Crafting Effective Press Releases");
                recommendations.put("Kinesthetic", "Public Speaking and Presentation Skills");
                break;
            case "Engineering":
                recommendations.put("Visual", "Blueprint Reading & Design Interpretation");
                recommendations.put("Auditory", "Engineering Podcasts and Seminars");
                recommendations.put("Reading/Writing", "Technical Documentation Writing");
                recommendations.put("Kinesthetic", "Hands-On Prototyping Workshop");
                break;
            case "Finance":
                recommendations.put("Visual", "Financial Modeling & Visualization");
                recommendations.put("Auditory", "Market Trends: Economic Insights");
                recommendations.put("Reading/Writing", "Investment Research & Reports");
                recommendations.put("Kinesthetic", "Budgeting Bootcamp: Practical Finance Tools");
                break;
            case "HSS SEA":
                recommendations.put("Visual", "3D Modeling of Surveillance Systems");
                recommendations.put("Auditory", "System Security Workshops");
                recommendations.put("Reading/Writing", "Security Protocol Documentation");
                recommendations.put("Kinesthetic", "System Installation and Maintenance");
                break;
            case "Human Resource":
                recommendations.put("Visual", "HR Analytics for Talent Management");
                recommendations.put("Auditory", "Leadership & Conflict Resolution");
                recommendations.put("Reading/Writing", "Policy Writing & Employee Handbook Development");
                recommendations.put("Kinesthetic", "Team-Building and Leadership Skills");
                break;
            case "Infocomm Technology":
                recommendations.put("Visual", "Data Visualization for Developers");
                recommendations.put("Auditory", "Technology Trends Roundtable Discussions");
                recommendations.put("Reading/Writing", "Technical Paper Reviews");
                recommendations.put("Kinesthetic", "Software Development Practices Workshop");
                break;
            case "Logistics":
                recommendations.put("Visual", "Supply Chain Management Tools");
                recommendations.put("Auditory", "Logistics Podcast Series");
                recommendations.put("Reading/Writing", "Logistics Process Documentation");
                recommendations.put("Kinesthetic", "Hands-On Supply Chain Management Exercises");
                break;
            case "Marketing":
                recommendations.put("Visual", "Branding and Graphic Design");
                recommendations.put("Auditory", "Marketing Strategies for Business Growth");
                recommendations.put("Reading/Writing", "Content Creation for Marketing");
                recommendations.put("Kinesthetic", "Social Media Engagement Practices");
                break;
            case "Procurement":
                recommendations.put("Visual", "Visual Analytics for Procurement");
                recommendations.put("Auditory", "Supplier Communication Workshops");
                recommendations.put("Reading/Writing", "Contract Management Resources");
                recommendations.put("Kinesthetic", "Hands-On Procurement Exercises");
                break;
            default:
                recommendations.put("Visual", "General Skill-Building Workshop");
                recommendations.put("Auditory", "Career Development Seminars");
                recommendations.put("Reading/Writing", "Self-Help Literature Recommendations");
                recommendations.put("Kinesthetic", "Experiential Learning Activities");
                break;
        }
        return recommendations;
    }
}
